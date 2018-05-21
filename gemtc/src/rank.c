#include "gemtc.h"

typedef struct Matrix {
    int * const data;
    int const nRow;
    int const nCol;
} Matrix;

/**
 * @param i Row index.
 * @param j Column index.
 */
static inline int *get(Matrix *m, int i, int j) {
    return m->data + j * (m->nRow) + i;
}

/**
 * Rank the n-array of doubles t, writing results to r.
 * The rank of t[i] is the number of elements greater than t[i].
 * Assumes that n is small (complexity O(n^2)).
 */
static inline void rank(double const *t, int *r, int n) {
	for (int i = 0; i < n; ++i) {
		r[i] = 0;
		for (int j = 0; j < n; ++j) {
			if (t[j] > t[i]) {
				++r[i];
			}
		}
	}
}

SEXP gemtc_rank_count(SEXP _t) {
	int const nIter = ncols(_t);
	int const nAlt = nrows(_t);

	_t = PROTECT(coerceVector(_t, REALSXP));
	double const *t = REAL(_t);

	SEXP _result = PROTECT(allocMatrix(INTSXP, nAlt, nAlt));
	Matrix c = { INTEGER(_result), nAlt, nAlt };
	for (int i = 0; i < nAlt; ++i) {
		for (int j = 0; j < nAlt; ++j) {
			*get(&c, i, j) = 0;
		}
	}

	int r[nAlt]; // alternative ranks
	for (int k = 0; k < nIter; ++k) {
		rank(t, r, nAlt); // rank the alternatives

		for (int i = 0; i < nAlt; ++i) {
			*get(&c, r[i], i) += 1; // update rank counts
		}

		t += nAlt;
	}

	UNPROTECT(2);
	return _result;
}
