package me.dvyy.nmr.bindings.helpers

enum class OS {
    WINDOWS, LINUX, MACOS;

    companion object {
        val current: OS by lazy {
            val osName = System.getProperty("os.name").lowercase()
            when {
                osName.contains("win") -> WINDOWS
                osName.contains("mac") -> MACOS
                osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> LINUX
                else -> throw IllegalStateException("Unsupported operating system: $osName")
            }
        }
    }
}