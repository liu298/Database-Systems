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
    target = []
    fd = fin[2+int(fin[1])].split("->")
    target.append(tuple(fd[0].strip().split(",")))
    target.append(fd[1].strip().split(","))

    return (attrs,fds,target)

def writelines(attrs,fds):
    ret = []
    fd_num = 0
    fd = []
    for key,val in fds.items():
        clos = computeClosure(key,fds)
        if set(key).issubset(set(attrs)): # contain this fd in relation
            rel_key = ",".join(str(i) for i in key)
            rel_val = [filter(lambda x: str(x) in attrs, sublist) for sublist in val]
            rel_val = filter(None,rel_val)
            rel_val = ",".join(rel_val)
            if len(rel_val) > 0: # there is non-None intersection between val and attrs
                fd_num += 1
                fd += [ rel_key +"->" + rel_val]
    ret += [",".join(str(i) for i in attrs)]
    ret += [str(fd_num)]+fd
    return "\n".join(ret)

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

def decompose(attrs,fds,target):
    """
    :type attrs: list
    :type fds: dict
    :type target: list(key,val)
    :rtype (r_num,r):(int,string)
    """
    ret = ""
    (key,val) = target
    key_clos = computeClosure(list(key),fds)
    r1_attr = sorted(list(key_clos))
    r2_attr = sorted(list(set(attrs)-set(key_clos))+list(key))
    if r1_attr < r2_attr:
        return writelines(r1_attr,fds) + "\n" + writelines(r2_attr,fds)
    else:
        return writelines(r2_attr,fds) + "\n" + writelines(r1_attr,fds)

if __name__ == "__main__":
    (attrs,fds,target) = lines()
    sys.stdout.write(decompose(attrs,fds,target))
