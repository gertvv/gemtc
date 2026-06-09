/* The contents of this file have been adapted from version 1.0-9 of the {truncnorm} package, retrieved from:
 *   https://github.com/olafmersmann/truncnorm/tree/28593c0b454bdbe860bc00e8e23690c12af89927
 * The package is credited to
 *   Olaf Mersmann, Heike Trautmann, Detlef Steuer and Bjorn Bornkamp
 * and is released under the GNU General Public Licence v2 (or later).
 * The package does not include a copy of that license, so it is not reproduced here.
 *
 * The superficial changes that led to this particular file were performed by Miguel Lechon on behalf of
 * Boehringer-Ingelheim Pharma GmbH & Co.KG. They do not deviate meaningfully from the original work, so no
 * separate copyright is claimed.
 */

/*
 * rtruncnorm.c - Random truncated normal number generator.
 *
 * Authors:
 *  Björn Bornkamp   <bornkamp@statistik.tu-dortmund.de>
 *  Olaf Mersmann    <olafm@statistik.uni-dortmund.de>
 */

#include <R.h>
#include <Rinternals.h>
#include <Rmath.h>

#define RTN_CHECK_ARG_IS_REAL_VECTOR(A)					\
    if (!isReal(A) || !isVector(A))					\
	error("Argument '" #A "' is not a real vector.");

#define RTN_CHECK_ARG_IS_INT_VECTOR(A)					\
    if (!isInteger(A) || !isVector(A))					\
	error("Argument '" #A "' is not an integer vector.");

/*
 * Unpack a real vector stored in SEXP S.
 */
#define RTN_UNPACK_REAL_VECTOR(S, D, N)             \
    RTN_CHECK_ARG_IS_REAL_VECTOR(S);		\
    double *D = REAL(S);			\
    const R_len_t N = length(S);                   

/*
 * Unpack a single integer stored in SEXP S.
 */
#define RTN_UNPACK_INT(S, I)			\
    RTN_CHECK_ARG_IS_INT_VECTOR(S);			\
    int I = INTEGER(S)[0];			\

#define RTN_ALLOC_REAL_VECTOR(S, D, N)                                             \
  SEXP S;                                                                      \
  PROTECT(S = allocVector(REALSXP, N));                                        \
  double *D = REAL(S);

#define RTN_MAX(A, B) ((A > B) ? (A) : (B))

#ifdef DEBUG
#define RTN_SAMPLER_DEBUG(N, A, B) Rprintf("%8s(%f, %f)\n", N, A, B)
#else
#define RTN_SAMPLER_DEBUG(N, A, B)
#endif

static const double RTN_t1 = 0.15;
static const double RTN_t2 = 2.18;
static const double RTN_t3 = 0.725;
static const double RTN_t4 = 0.45;

/* Exponential rejection sampling (a,inf) */
static R_INLINE double RTN_ers_a_inf(double a) {
  RTN_SAMPLER_DEBUG("RTN_ers_a_inf", a, R_PosInf);
  const double ainv = 1.0 / a;
  double x, rho;
  do {
    x = rexp(ainv) + a; /* rexp works with 1/lambda */
    rho = exp(-0.5 * pow((x - a), 2));
  } while (runif(0, 1) > rho);
  return x;
}

/* Exponential rejection sampling (a,b) */
static R_INLINE double RTN_ers_a_b(double a, double b) {
  RTN_SAMPLER_DEBUG("RTN_ers_a_b", a, b);
  const double ainv = 1.0 / a;
  double x, rho;
  do {
    x = rexp(ainv) + a; /* rexp works with 1/lambda */
    rho = exp(-0.5 * pow((x - a), 2));
  } while (runif(0, 1) > rho || x > b);
  return x;
}

/* Normal rejection sampling (a,b) */
static R_INLINE double RTN_nrs_a_b(double a, double b) {
  RTN_SAMPLER_DEBUG("RTN_nrs_a_b", a, b);
  double x = -DBL_MAX;
  while (x < a || x > b) {
    x = rnorm(0, 1);
  }
  return x;
}

/* Normal rejection sampling (a,inf) */
static R_INLINE double RTN_nrs_a_inf(double a) {
  RTN_SAMPLER_DEBUG("RTN_nrs_a_inf", a, R_PosInf);
  double x = -DBL_MAX;
  while (x < a) {
    x = rnorm(0, 1);
  }
  return x;
}

/* Half-normal rejection sampling */
static double RTN_hnrs_a_b(double a, double b) {
  RTN_SAMPLER_DEBUG("RTN_hnrs_a_b", a, b);
  double x = a - 1.0;
  while (x < a || x > b) {
    x = rnorm(0, 1);
    x = fabs(x);
  }
  return x;
}

/* Uniform rejection sampling */
static R_INLINE double RTN_urs_a_b(double a, double b) {
  RTN_SAMPLER_DEBUG("RTN_urs_a_b", a, b);
  const double phi_a = dnorm(a, 0.0, 1.0, FALSE);
  double x = 0.0;

  /* Upper bound of normal density on [a, b] */
  const double ub = a < 0 && b > 0 ? M_1_SQRT_2PI : phi_a;
  do {
    x = runif(a, b);
  } while (runif(0, 1) * ub > dnorm(x, 0, 1, 0));
  return x;
}

/* Previously this was refered to as type 1 sampling: */
static inline double RTN_r_lefttruncnorm(double a, double mean, double sd) {
  const double alpha = (a - mean) / sd;
  if (alpha < RTN_t4) {
    return mean + sd * RTN_nrs_a_inf(alpha);
  } else {
    return mean + sd * RTN_ers_a_inf(alpha);
  }
}

static R_INLINE double RTN_r_righttruncnorm(double b, double mean, double sd) {
  const double beta = (b - mean) / sd;
  /* Exploit symmetry: */
  return mean - sd * RTN_r_lefttruncnorm(-beta, 0.0, 1.0);
}

static R_INLINE double RTN_r_truncnorm(double a, double b, double mean, double sd) {
  const double alpha = (a - mean) / sd;
  const double beta = (b - mean) / sd;
  const double phi_a = dnorm(alpha, 0.0, 1.0, FALSE);
  const double phi_b = dnorm(beta, 0.0, 1.0, FALSE);
  if (beta <= alpha) {
    return NA_REAL;
  } else if (alpha <= 0 && 0 <= beta) { /* 2 */
    if (phi_a <= RTN_t1 || phi_b <= RTN_t1) {   /* 2 (a) */
      return mean + sd * RTN_nrs_a_b(alpha, beta);
    } else { /* 2 (b) */
      return mean + sd * RTN_urs_a_b(alpha, beta);
    }
  } else if (alpha > 0) {      /* 3 */
    if (phi_a / phi_b <= RTN_t2) { /* 3 (a) */
      return mean + sd * RTN_urs_a_b(alpha, beta);
    } else {
      if (alpha < RTN_t3) { /* 3 (b) */
        return mean + sd * RTN_hnrs_a_b(alpha, beta);
      } else { /* 3 (c) */
        return mean + sd * RTN_ers_a_b(alpha, beta);
      }
    }
  } else {                     /* 3s */
    if (phi_b / phi_a <= RTN_t2) { /* 3s (a) */
      return mean - sd * RTN_urs_a_b(-beta, -alpha);
    } else {
      if (beta > -RTN_t3) { /* 3s (b) */
        return mean - sd * RTN_hnrs_a_b(-beta, -alpha);
      } else { /* 3s (c) */
        return mean - sd * RTN_ers_a_b(-beta, -alpha);
      }
    }
  }
}

SEXP C_do_rtruncnorm(SEXP s_n, SEXP s_a, SEXP s_b, SEXP s_mean, SEXP s_sd) {
  R_len_t i, nn;
  RTN_UNPACK_INT(s_n, n);
  if (NA_INTEGER == n)
    error("n is NA - aborting.");
  RTN_UNPACK_REAL_VECTOR(s_a, a, n_a);
  RTN_UNPACK_REAL_VECTOR(s_b, b, n_b);
  RTN_UNPACK_REAL_VECTOR(s_mean, mean, n_mean);
  RTN_UNPACK_REAL_VECTOR(s_sd, sd, n_sd);

  nn = RTN_MAX(n, RTN_MAX(RTN_MAX(n_a, n_b), RTN_MAX(n_mean, n_sd)));
  RTN_ALLOC_REAL_VECTOR(s_ret, ret, nn);

  GetRNGstate();
  for (i = 0; i < nn; ++i) {
    const double ca = a[i % n_a];
    const double cb = b[i % n_b];
    const double cmean = mean[i % n_mean];
    const double csd = sd[i % n_sd];

    if (R_FINITE(ca) && R_FINITE(cb)) {
      ret[i] = RTN_r_truncnorm(ca, cb, cmean, csd);
    } else if (R_NegInf == ca && R_FINITE(cb)) {
      ret[i] = RTN_r_righttruncnorm(cb, cmean, csd);
    } else if (R_FINITE(ca) && R_PosInf == cb) {
      ret[i] = RTN_r_lefttruncnorm(ca, cmean, csd);
    } else if (R_NegInf == ca && R_PosInf == cb) {
      ret[i] = rnorm(cmean, csd);
    } else {
      ret[i] = NA_REAL;
    }
    R_CheckUserInterrupt();
  }
  PutRNGstate();
  UNPROTECT(1); /* s_ret */
  return s_ret;
}

#undef RTN_CHECK_ARG_IS_REAL_VECTOR
#undef RTN_CHECK_ARG_IS_INT_VECTOR
#undef RTN_UNPACK_REAL_VECTOR
#undef RTN_UNPACK_INT
#undef RTN_ALLOC_REAL_VECTOR
#undef RTN_MAX
#undef RTN_SAMPLER_DEBUG
