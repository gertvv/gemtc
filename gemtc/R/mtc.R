####
filter.parameters <- function(parameters, criterion) { 
    parameters <- lapply(parameters, function(x) { 
    path <- unlist(strsplit(x, '\\.')) 
    if(criterion(path)) { 
        path[-1]
    }})
    parameters[!sapply(parameters, is.null)]
}

mtc.spanning.tree <- function(parameters, network) {
    parameters <- unlist(filter.parameters(parameters, function(x) { x[1] == 'd' }))
    parameters <- matrix(parameters, nrow=2)
    t1 <- factor(parameters[1,], levels=levels(network$treatments$id))
    t2 <- factor(parameters[2,], levels=levels(network$treatments$id))
    parameters <- data.frame(t1=t1, t2=t2)
    graph.create(network$treatments$id, parameters, arrow.mode=2, color="black", lty=1)
}

w.factors <- function(parameters, network) {
    basic <- do.call(rbind, filter.parameters(parameters, function(x) { x[1] == 'd' }))
    extract.unique <- function(f, basic) {
        f <- c(f, f[1])
        factors <- lapply(1:length(f), function(x, pars) { c(pars[x - 1], pars[x]) }, f)[-1]
        factors <- do.call(rbind, factors)
        apply(factors, 1, function(fac) {
            if (!any(basic[,1]==fac[1] & basic[,2] == fac[2]) &&
                !any(basic[,2]==fac[1] & basic[,1] == fac[2])) {
                fac
            } else NULL
        })
    }
    w.factors <- filter.parameters(parameters, function(x) { x[1] == 'w' })
    w.factors <- unlist(lapply(w.factors, extract.unique, basic), recursive=FALSE)
    w.factors <- matrix(unlist(w.factors), nrow=2)
    data.frame(
        t1 = as.treatment.factor(w.factors[1,], network),
        t2 = as.treatment.factor(w.factors[2,], network))
}
