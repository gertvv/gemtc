deviance.binom <-
"rhat[i, k] <- p[i, k] * n[i, k]
dev[i, k] <- 2 * (r[i, k] * (log(r[i, k]) - log(rhat[i, k])) +
                  (n[i, k]-r[i, k]) * (log(n[i, k] - r[i, k]) - log(n[i, k] - rhat[i, k])))"

deviance.poisson <-
"dev[i, k] <- 2 * ((theta[i, k] - r[i, k]) + r[i, k] * log(r[i, k]/theta[i, k]))"

deviance.normal <-
"dev[i, k] <- pow(m[i, k] - theta[i, k], 2) * prec[i, k]"
