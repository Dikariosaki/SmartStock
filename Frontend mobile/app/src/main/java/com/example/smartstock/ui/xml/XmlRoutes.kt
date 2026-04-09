package com.example.smartstock.ui.xml

import com.example.smartstock.core.normalizeRoleName

object XmlRoutes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val USERS = "users"
    const val PRODUCTS = "products"
    const val TASKS = "tasks"
    const val INVENTORY = "inventory"
    const val MOVEMENTS = "movements"
    const val REPORTS = "reports"
    const val PROFILE = "profile"
}

fun canAccessXmlRoute(roleName: String?, route: String): Boolean {
    val role = normalizeRoleName(roleName)
    return when (route) {
        XmlRoutes.LOGIN -> true
        XmlRoutes.HOME, XmlRoutes.PROFILE -> role != null
        XmlRoutes.USERS -> role == "administrador"
        XmlRoutes.PRODUCTS, XmlRoutes.TASKS -> role in setOf("administrador", "supervisor", "auxiliar")
        XmlRoutes.INVENTORY, XmlRoutes.MOVEMENTS -> role in setOf("administrador", "supervisor")
        XmlRoutes.REPORTS -> role in setOf("administrador", "supervisor", "auxiliar")
        else -> false
    }
}
