import sys
def readlines():
    fin = sys.stdin.readlines()
    attrs = []
    fds = {}
    attrs = fin[0].strip().split(",")
    for i in range(2,len(fin)):
        if len(fin[i].strip())!= 0:
            fd = fin[i].split("->")
            key = tuple(fd[0].strip().split(","))
            val = fd[1].strip().split(",")
            if key not in fds:
                fds[key] = val
            else:
                fds[key] += val
    return (attrs,fds)

def computeClosure(attr,fds):
    """
    :type attr: list
    :type fds: dict
    :rtype list
    """
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

def isBCNF(attrs,fds):
	"""
	:type attrs: list
	:type fds: dict
	:rtype bool
	"""
	for key,val in fds.items():
		key_clos = computeClosure(list(key),fds) 
		if set(key_clos).issubset(set(attrs)) and set(key_clos)!=set(attrs):
			return False
	return True

if __name__ == "__main__":
    (attrs,fds) = readlines()
    if isBCNF(attrs,fds):
    	sys.stdout.write("Yes")
    else:
    	sys.stdout.write("No")

