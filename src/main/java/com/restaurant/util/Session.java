package com.restaurant.util;

import com.restaurant.model.Employee;

public class Session {
    private static Employee current;
    public static Employee getCurrent() { return current; }
    public static void setCurrent(Employee e) { current = e; }
    public static void clear() { current = null; }
}
