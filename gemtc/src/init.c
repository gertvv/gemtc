#include "gemtc.h"
#include <R_ext/Visibility.h>

static const R_CallMethodDef callMethods[] = {
  { "gemtc_rank_count", (DL_FUNC) &gemtc_rank_count, 1 },
  { NULL, NULL, 0 }
};

void attribute_visible R_init_gemtc(DllInfo *dll) {
  R_registerRoutines(dll, NULL, callMethods, NULL, NULL);
  R_useDynamicSymbols(dll, FALSE);
  R_forceSymbols(dll, TRUE);
}
