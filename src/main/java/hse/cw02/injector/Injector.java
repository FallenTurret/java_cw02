package hse.cw02.injector;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;


public class Injector {

    private static Map<String, Object> mapToInstance;
    private static int level = 0;

    /**
     * Create and initialize object of `rootClassName` class using classes from
     * `implementationClassNames` for concrete dependencies.
     */
    public static Object initialize(String rootClassName, List<String> implementationClassNames) throws Exception {
        if (level == 0) {
            mapToInstance = new HashMap<String, Object>();
        }
        var toCreate = Class.forName(rootClassName);
        var constructor = toCreate.getDeclaredConstructors()[0];
        var arguments = Arrays.stream(constructor.getParameterTypes())
                .map(Type::getTypeName).toArray(String[]::new);
        for (var arg: arguments) {
            var name = arg;
            if (mapToInstance.containsKey(name)) {
                if (mapToInstance.get(name) == null) {
                    throw new InjectionCycleException();
                }
                continue;
            }
            var argClass = Class.forName(name);
            String implName = null;
            if (argClass.isInterface()) {
                boolean found = false;
                for (var names: implementationClassNames) {
                    var interfaces = Arrays.stream(Class.forName(names).getInterfaces())
                            .map(Class::getName).collect(Collectors.toSet());
                    if (interfaces.contains(name)) {
                        if (found) {
                            throw new AmbiguousImplementationException();
                        }
                        found = true;
                        implName = names;
                    }
                }
                if (!found) {
                    throw new ImplementationNotFoundException();
                }
            } else {
                boolean found = false;
                for (var names: implementationClassNames) {
                    if (names.equals(name)) {
                        found = true;
                        implName = name;
                    }
                }
                if (!found) {
                    throw new ImplementationNotFoundException();
                }
            }
            mapToInstance.put(implName, null);
            level++;
            var instance = initialize(implName, implementationClassNames);
            level--;
            mapToInstance.replace(implName, instance);
            if (argClass.isInterface()) {
                mapToInstance.put(name, instance);
            }
        }
        var instances = new ArrayList<Object>();
        for (var arg: arguments) {
            instances.add(mapToInstance.get(arg));
        }
        return constructor.newInstance(instances.toArray(new Object[instances.size()]));
    }
}