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
    		

def writelines(attrs,fds):
	ret = []
	fd_num = 0
	fd = []
	for key,val in fds.items():
		clos = computeClosure(key,fds)
		if set(clos).issubset(set(attrs)): # contain this fd in relation
			fd_num += 1
			fd += [",".join(str(i) for i in key) +"->" + ",".join(str(i) for i in val)]
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
	# print values
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


def decompose(attrs,fds):
	"""
	:type attrs: list
	:type fds: dict
	:rtype (r_num,r):(int,string)
	"""
	ret = ""
	for key,val in fds.items():
		key_clos = computeClosure(list(key),fds) 
		if set(key_clos).issubset(set(attrs)) and set(key_clos)!=set(attrs):
			# find fds that violate BCNF and decompose
			r1 = decompose(key_clos,fds)
			n1 = int(r1.split()[0])
			r1 = "\n".join(r1.split()[1:])
			attr2 = list(set(attrs)-set(key_clos))+list(key)
			r2 = decompose(attr2,fds)
			n2 = int(r2.split()[0])
			r2 = "\n".join(r2.split()[1:])
			ret += str(n1+n2)+"\n"+r1+"\n"+r2
			return ret
	ret += "1\n"+ writelines(attrs,fds)
	return ret


def main():
	# my own test cases
	attr = ["a","b","c","d","e"]
	fds = {("c","b"):"a","a":"e",("d","e"):"b"}
	print computeClosure(["b","c"],fds)

	# test cases through file
	(attrs,fds) = readlines(sys.argv[1])
	if isBCNF(attrs,fds):
		print "YES"
	else:
		print "NO\n"+decompose(attrs,fds)


if __name__ == '__main__':
	main()