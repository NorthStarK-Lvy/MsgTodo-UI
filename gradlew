#!/bin/sh
# ClassNoti Gradle wrapper（POSIX / Git Bash）。
#
# 两件事：
#   1. Git Bash 下 $(pwd) 是 POSIX 路径（/d/...），JVM 认不出来，必须转成 Windows 路径。
#   2. 支持把依赖缓存放到别的盘（不占 C 盘）—— 具体路径写在**不入库**的
#      `gradle-home.local` 里，这样仓库本身不含任何个人路径。
DIR=$(cd "$(dirname "$0")" && pwd)
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*)
    if command -v cygpath >/dev/null 2>&1; then
      DIR=$(cygpath -m "$DIR")
    fi
    ;;
esac

# 可选的本地覆盖（**不入库**，见 .gitignore）：
# 在里面写一行 `GRADLE_USER_HOME=/d/your/cache`，即可把 Gradle 缓存放到指定盘。
# 外部已显式设置 GRADLE_USER_HOME 时以外部为准，不覆盖。
if [ -z "$GRADLE_USER_HOME" ] && [ -f "$DIR/gradle-home.local" ]; then
  # shellcheck disable=SC1090
  . "$DIR/gradle-home.local"
  export GRADLE_USER_HOME
fi

if [ -n "$JAVA_HOME" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD=java
fi

exec "$JAVACMD" -classpath "$DIR/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain "$@"
