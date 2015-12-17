#' @param obj Numeric vector of objective coefficients
#' @param mat Numeric matrix of constraint coefficients
#' @param rhs Numeric vector of constraint right-hand sides
#' @param eq Logical vector; TRUE for equality, FALSE for <=
#' @param max Logical scalar; TRUE for maximize, FALSE for minimize
solveLP <- function(obj, mat, rhs, eq, max=FALSE) {
  # Solution using RCDD (results in memory corruption...)
  # constraints <- cbind(eq, rhs, -mat)
  # sol <- rcdd::lpcdd(constraints, obj, minimize=!max)
  # if (sol$solution.type == "Optimal") {
  #   sol$optimal.value
  # } else if (sol$solution.type == "DualInconsistent" || sol$solution.type == "StrucDualInconsistent") {
  #   if (max) { +Inf } else { -Inf }
  # } else {
  #   stop(paste("LP solver:", sol$solution.type))
  # }

  # Solution using Rglpk
  GLP_OPT <- 5    # solution is optimal
  GLP_UNBND <- 6  # solution is unbounded
  status <- c("solution is undefined", "solution is feasible", "solution is infeasible", "no feasible solution exists", "solution is optimal", "solution is unbounded")

  dir <- c("<=", "==")[eq + 1]
  m <- ncol(mat)
  # Explicitly set bounds to (-Inf, +Inf) - the default is [0, +Inf)
  bounds <- list(lower=list(ind=1:m, val=rep(-Inf, m)),
                 upper=list(ind=1:m, val=rep(+Inf, m)))
  sol <- Rglpk::Rglpk_solve_LP(obj, mat, dir, rhs, max=max, bounds=bounds, control = list(canonicalize_status=FALSE))
  if (sol$status == GLP_OPT) {
    sol$optimum
  } else if (sol$status == GLP_UNBND) {
    if (max) { +Inf } else { -Inf }
  } else {
    stop(paste("LP solver:", status[sol$status]))
  }
}
