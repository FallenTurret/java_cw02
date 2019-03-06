package hse.cw02.injector;


import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class TestInjector {

    @Test
    public void injectorShouldInitializeClassWithoutDependencies()
            throws Exception {
        Object object = Injector.initialize("hse.cw02.injector.ClassWithoutDependencies", Collections.emptyList());
        assertTrue(object instanceof ClassWithoutDependencies);
    }

    @Test
    public void injectorShouldInitializeClassWithOneClassDependency()
            throws Exception {
        Object object = Injector.initialize(
                "hse.cw02.injector.ClassWithOneClassDependency",
                Collections.singletonList("hse.cw02.injector.ClassWithoutDependencies")
        );
        assertTrue(object instanceof ClassWithOneClassDependency);
        ClassWithOneClassDependency instance = (ClassWithOneClassDependency) object;
        assertTrue(instance.dependency != null);
    }

    @Test
    public void injectorShouldInitializeClassWithOneInterfaceDependency()
            throws Exception {
        Object object = Injector.initialize(
                "hse.cw02.injector.ClassWithOneInterfaceDependency",
                Collections.singletonList("hse.cw02.injector.InterfaceImpl")
        );
        assertTrue(object instanceof ClassWithOneInterfaceDependency);
        ClassWithOneInterfaceDependency instance = (ClassWithOneInterfaceDependency) object;
        assertTrue(instance.dependency instanceof InterfaceImpl);
    }

    @Test
    public void injectorShouldFindCyclicDependencies() {
        var list = new ArrayList<String>();
        list.add("hse.cw02.injector.Cycle1");
        list.add("hse.cw02.injector.Cycle2");
        assertThrows(InjectionCycleException.class, () -> {
            Injector.initialize("hse.cw02.injector.Cycle1", list);
        });
    }

    @Test
    public void injectorShouldFindMissingDependencies() {
        var list = new ArrayList<String>();
        list.add("hse.cw02.injector.Cycle1");
        assertThrows(ImplementationNotFoundException.class, () -> {
            Injector.initialize("hse.cw02.injector.Cycle1", list);
        });
    }

    @Test
    public void injectorShouldFindAmbiguousDependencies() {
        var list = new ArrayList<String>();
        list.add("hse.cw02.injector.Interface");
        list.add("hse.cw02.injector.InterfaceImpl");
        list.add("hse.cw02.injector.InterfaceImpl2");
        list.add("hse.cw02.injector.ClassWithOneInterfaceDependency");
        assertThrows(AmbiguousImplementationException.class, () -> {
            Injector.initialize("hse.cw02.injector.ClassWithOneInterfaceDependency", list);
        });
    }
}
