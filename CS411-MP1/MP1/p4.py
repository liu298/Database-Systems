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

def writelines(attrs,fds):
    ret = []
    fd_num = 0
    fd = []
    for key in sorted(fds.keys()):
        val = fds[key]
        clos = computeClosure(key,fds)
        if set(key).issubset(set(attrs)): # contain this fd in relation
            rel_key = ",".join(str(i) for i in sorted(key))
            #rel_val = [filter(lambda x: str(x) in attrs, sublist) for sublist in val]
            rel_val = [str(x) if x in attrs else "" for x in val]
            rel_val = filter(None,rel_val)
            rel_val = ",".join(sorted(rel_val))
            if len(rel_val) > 0: # there is non-None intersection between val and attrs
                fd_num += 1
                fd += [ rel_key +"->" + rel_val]
    ret += [",".join(str(i) for i in sorted(attrs))]
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

def helper_decompose(attrs,fds):
    """
    :type attrs: list
    :type fds: dict
    :type target: list(key,val)
    :rtype list:list of relations
    """
    for key,val in fds.items():
        key_clos = computeClosure(list(key),fds) 
        if set(key_clos).issubset(set(attrs)) and set(key_clos)!=set(attrs):
            # find fds that violate BCNF and decompose
            (n1,r1_relation) = helper_decompose(key_clos,fds)
            r2_attr = sorted(list(set(attrs)-set(key_clos))+list(key))
            (n2,r2_relation) = helper_decompose(r2_attr,fds)
            num_relation = n1+n2
            relations = sorted([r1_relation]+[r2_relation])
            return (num_relation,relations)
    return (1,attrs)

def decompose(attrs,fds):
    ret = ""
    for key,val in fds.items():
        key_clos = computeClosure(list(key),fds) 
        if set(key_clos).issubset(set(attrs)) and set(key_clos)!=set(attrs):
            # find fds that violate BCNF and decompose
            (n1,r1_relation) = helper_decompose(key_clos,fds)
            r2_attr = sorted(list(set(attrs)-set(key_clos))+list(key))
            (n2,r2_relation) = helper_decompose(r2_attr,fds)
            num_relation = n1+n2
            relations = [r1_relation]+[r2_relation]
            flat_relations = []
            for val in relations:
                if isinstance(val[0],str):
                    flat_relations += [val]
                else:
                    for i in val:
                        flat_relations += [i]
            writeRel = "\n".join([writelines(i,fds) for i in sorted(flat_relations)])
            ret += str(num_relation) + "\n" + writeRel
            return ret

if __name__ == "__main__":
    (attrs,fds) = lines()
    sys.stdout.write(decompose(attrs,fds))