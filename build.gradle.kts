// 顶层构建脚本。
// 版本组合与上游 MsgTodo 项目保持一致（在该组合上完整验证过）：
//   Gradle 9.6.1 / AGP 9.3.1 / Kotlin 2.4.10
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:9.3.1")
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.4.10")
    }
}
