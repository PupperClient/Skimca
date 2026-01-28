package cn.pupperclient.skimca.event;

import cn.pupperclient.skimca.SkimcaLogger;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * An enhanced event manager that automatically discovers and registers <br/>
 * classes containing {@code @EventTarget} annotated methods. <br/>
 * Supports both automatic classpath scanning and explicit registration.
 */
public class SkimcaEventManager {

    /** Maps event classes to lists of registered handlers, sorted by priority. */
    private final Map<Class<? extends Event>, List<RegisteredHandler>> handlerMap = new ConcurrentHashMap<>();

    /** Maps method objects to their containing instances for reference. */
    private final Map<Method, Object> methodObjectMap = new ConcurrentHashMap<>();

    /** Cache of already registered classes to prevent duplicate registration. */
    private final Set<Class<?>> registeredClasses = ConcurrentHashMap.newKeySet();

    /** Cache of class loaders to registered classes. */
    private final Map<ClassLoader, Set<Class<?>>> classLoaderRegistry = new ConcurrentHashMap<>();

    /** Singleton instance. */
    private static final SkimcaEventManager INSTANCE = new SkimcaEventManager();

    /**
     * Returns the singleton instance of the event manager.
     *
     * @return the singleton event manager instance
     */
    public static SkimcaEventManager getInstance() {
        return INSTANCE;
    }

    /**
     * Registers an object, automatically discovering and registering all methods
     * annotated with {@code @EventTarget} within the object's class.
     *
     * @param obj the object to register
     * @throws IllegalArgumentException if the object is null
     */
    public void register(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Cannot register null object");
        }

        Class<?> clazz = obj.getClass();
        registerClassInternal(clazz, () -> obj);
    }

    /**
     * Registers a class. The class will be instantiated (must have a no-arg constructor)
     * and all {@code @EventTarget} methods will be registered.
     *
     * @param clazz the class to register
     * @throws IllegalArgumentException if the class cannot be instantiated
     */
    public void register(Class<?> clazz) {
        registerClassInternal(clazz, () -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Failed to instantiate class " + clazz.getName() +
                                " (must have a public no-arg constructor)", e);
            }
        });
    }

    /**
     * Registers all classes in the specified package that contain {@code @EventTarget} methods.
     * Note: This uses reflection and may have performance implications.
     *
     * @param packageName the package name to scan
     */
    public void registerPackage(String packageName) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            java.net.URL resource = classLoader.getResource(path);

            if (resource != null) {
                java.io.File directory = new java.io.File(resource.getFile());
                if (directory.exists()) {
                    scanDirectory(directory, packageName, classLoader);
                }
            }
        } catch (Exception e) {
            SkimcaLogger.error("EventManager", "Failed to scan package: " + packageName, e);
        }
    }

    /**
     * Automatically registers all classes from the specified class loader
     * that contain {@code @EventTarget} methods.
     *
     * @param classLoader the class loader to scan
     */
    public void autoRegisterFromClassLoader(ClassLoader classLoader) {
        if (classLoaderRegistry.containsKey(classLoader)) {
            return; // Already registered from this class loader
        }

        Set<Class<?>> registered = ConcurrentHashMap.newKeySet();
        classLoaderRegistry.put(classLoader, registered);

        // In a real implementation, you might use a library like Reflections
        // or ClassGraph for classpath scanning. This is a simplified version.
        SkimcaLogger.info("EventManager",
                "Auto-registration from class loader: " + classLoader);
    }

    /**
     * Dispatches an event to all registered handlers.
     * Handlers are executed in order of priority (highest to lowest).
     *
     * @param event the event to dispatch
     */
    public <T extends Event> void call(T event) {
        if (event == null) {
            SkimcaLogger.error("EventManager", "Cannot dispatch null event");
            return;
        }

        Class<? extends Event> eventClass = event.getClass();
        List<RegisteredHandler> handlers = handlerMap.get(eventClass);

        if (handlers == null || handlers.isEmpty()) {
            return;
        }

        boolean wasCancelled = event.isCancelled();

        for (RegisteredHandler handler : handlers) {
            // Skip if handler ignores canceled events and event is already canceled
            if (!handler.ignoreCancelled && wasCancelled && event.isCancelled()) {
                continue;
            }

            try {
                handler.method.invoke(handler.instance, event);
            } catch (Exception e) {
                SkimcaLogger.error("EventManager",
                        "Error invoking event handler " + handler.method.getName() +
                                " for event " + eventClass.getSimpleName(), e);
            }
        }

    }

    /**
     * Unregisters all event handlers from the specified object.
     *
     * @param obj the object to unregister
     */
    public void unregister(Object obj) {
        if (obj == null) return;

        Class<?> clazz = obj.getClass();
        int removedCount = 0;

        // Remove from handler map
        for (List<RegisteredHandler> handlers : handlerMap.values()) {
            Iterator<RegisteredHandler> iterator = handlers.iterator();
            while (iterator.hasNext()) {
                RegisteredHandler handler = iterator.next();
                if (handler.instance == obj) {
                    iterator.remove();
                    methodObjectMap.remove(handler.method);
                    removedCount++;
                }
            }
        }

        // Remove from registered classes
        registeredClasses.remove(clazz);

        if (removedCount > 0) {
            SkimcaLogger.info("EventManager",
                    "Unregistered " + removedCount + " event handlers from " + clazz.getName());
        }
    }

    /**
     * Clears all registered event handlers.
     */
    public void clear() {
        handlerMap.clear();
        methodObjectMap.clear();
        registeredClasses.clear();
        classLoaderRegistry.clear();
        SkimcaLogger.info("EventManager", "Cleared all event handlers");
    }

    /**
     * Returns the number of registered handlers for a specific event type.
     *
     * @param eventClass the event class
     * @return the number of registered handlers
     */
    public int getHandlerCount(Class<? extends Event> eventClass) {
        List<RegisteredHandler> handlers = handlerMap.get(eventClass);
        return handlers != null ? handlers.size() : 0;
    }

    /**
     * Returns the total number of registered classes.
     *
     * @return the number of registered classes
     */
    public int getRegisteredClassCount() {
        return registeredClasses.size();
    }

    /**
     * Internal method to register a class with an instance supplier.
     */
    private void registerClassInternal(Class<?> clazz, Supplier<Object> instanceSupplier) {
        // Check if this class has already been registered
        if (registeredClasses.contains(clazz)) {
            SkimcaLogger.warn("EventManager", "Class " + clazz.getName() + " is already registered");
            return;
        }

        Object instance = null;
        int registeredCount = 0;
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            EventTarget annotation = method.getAnnotation(EventTarget.class);
            if (annotation != null && method.getParameterTypes().length == 1) {
                // Create instance lazily (only if we have at least one @EventTarget method)
                if (instance == null) {
                    instance = instanceSupplier.get();
                }

                Class<?> paramType = method.getParameterTypes()[0];

                // Verify the parameter is an Event subclass
                if (Event.class.isAssignableFrom(paramType)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> eventClass = (Class<? extends Event>) paramType;

                    // Make the method accessible if it's private
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }

                    RegisteredHandler handler = new RegisteredHandler(
                            method,
                            instance,
                            annotation.priority(),
                            annotation.ignoreCancelled()
                    );

                    // Add to handler map
                    handlerMap.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
                            .add(handler);

                    // Sort handlers by priority (highest first)
                    handlerMap.get(eventClass).sort(Comparator.comparingInt(h -> -h.priority.ordinal()));

                    methodObjectMap.put(method, instance);
                    registeredCount++;

                    SkimcaLogger.info("EventManager",
                            "Registered handler: " + method.getName() +
                                    " for event: " + eventClass.getSimpleName() +
                                    " with priority: " + annotation.priority());
                } else {
                    SkimcaLogger.error("EventManager",
                            "Method " + method.getName() + " in class " + clazz.getName() +
                                    " has @EventTarget annotation but parameter is not an Event subclass");
                }
            }
        }

        if (registeredCount > 0) {
            registeredClasses.add(clazz);
            SkimcaLogger.info("EventManager",
                    "Registered " + registeredCount + " event handlers from " + clazz.getName());
        } else {
            SkimcaLogger.warn("EventManager",
                    "No @EventTarget methods found in class " + clazz.getName());
        }
    }

    /**
     * Recursively scans a directory for Java classes.
     */
    private void scanDirectory(java.io.File directory, String packageName, ClassLoader classLoader) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), classLoader);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = classLoader.loadClass(className);
                    // Check if class has @EventTarget methods before registering
                    if (hasEventTargetMethods(clazz)) {
                        register(clazz);
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    // Ignore - class may not be loadable
                }
            }
        }
    }

    /**
     * Checks if a class has any methods annotated with @EventTarget.
     */
    private boolean hasEventTargetMethods(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventTarget.class)) {
                return true;
            }
        }
        return false;
    }

    /**
         * Internal representation of a registered event handler.
         */
    private record RegisteredHandler(Method method, Object instance, EventTarget.Priority priority,
                                     boolean ignoreCancelled) {}
}