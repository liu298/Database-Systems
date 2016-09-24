import sys

def readlines(file):
	attrs = []
	fds = {}
	with open(file, 'r') as fin:
		fin = fin.readlines()
		attrs = fin[0].strip().split(",")
		for i in range(2,len(fin)):
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
	closure = [at for at in attr]
	for key,val in fds.items():
		if set(key).issubset(set(closure)):
			closure += [at for at in val]
			closure = list(set(closure))
	return closure

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
	# attr = ["a","b","c"]
	# attrs = ["a","b","c","d","e","f","g"]
	# fds = {"a":"b","b":"c",("a","d"):["e","f"],("a","b","d"):["g","f"]}
	# fd2 = {"a":"b"}
	# # print computeClosure(attr,fds)
	# # print isBCNF(attrs,fd2)
	(attrs,fds) = readlines(sys.argv[1])
	if isBCNF(attrs,fds):
		print "YES"
	else:
		print "NO\n"+decompose(attrs,fds)


if __name__ == '__main__':
	main()