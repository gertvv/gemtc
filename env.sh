# Modify this script to correctly find JAGS-JNI and JAGS modules, then source
# it into your shell (`. env.sh')

JAGS_JNI="../jags-jni/src"
JAGS_MODULES="/usr/local/lib/JAGS/modules-2.0.0"

export LD_LIBRARY_PATH="${JAGS_JNI}:${JAGS_MODULES}:"
