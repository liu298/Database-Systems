# Enter your code here. Read input from STDIN. Print output to STDOUT
import sys
def lines():
    attrs = []
    fds = {}
    fin = sys.stdin.readlines()
    attrs = fin[0].strip().split(",")
    for i in range(2,2+int(fin[1])):
        fd = fin[i].split("->")
        key = tuple(fd[0].strip().split(","))
        val = fd[1].strip().split(",")
        if key not in fds:
            fds[key] = val
        else:
            fds[key] += val
    return (attrs,fds)

def computeClosure(attr,fds):
    clo = [at for at in attr]
    use = []
    junk = []
    poten = range(len(fds))
    values = []
    for val in fds.values():
        values.extend(val)
    values = list(set(values))
    while(len(use)+len(junk)!=len(fds)):
        prev = clo
        for k in poten:
            (clo,use,junk,poten) = isUseful(k,clo,fds,values,use,junk,poten)
            if(len(use)+len(junk)==len(fds)):
                return clo
        if(set(clo)==set(prev)):
            return clo
    return clo

def isUseful(k,clo,fds,values,use,junk,poten):
    if set(fds.keys()[k]).issubset(set(clo)):
        clo += [att for att in fds.values()[k]]
        clo = list(set(clo))
        use.append(k)
        return (clo,use,junk,poten)
    else:
        diff = set(fds.keys()[k])-set(clo)
        if(not set(diff).issubset(set(values))):
            junk.append(k)
            return (clo,use,junk,poten)
        else:
            poten.append(k)
            poten = list(set(poten))
            return (clo,use,junk,poten)

if __name__ == "__main__":
    (attrs,fds) = lines()
    clo = computeClosure(attrs,fds)
    sys.stdout.write(",".join(sorted(clo)))
