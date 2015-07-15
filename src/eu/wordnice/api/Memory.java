package eu.wordnice.api;

public class Memory {
	
	
	public static boolean memcpy(long to, long from, long sz) {
		return Api.getUnsafe().callMethod("copyMemory", from, to, sz) != null;
	}
	
	public static boolean memset(long to, byte val, long sz) {
		return Api.getUnsafe().callMethod("setMemory", to, sz, val) != null;
	}
	
	
	public static long malloc(long sz) {
		Val.OneVal<Object> valr = Api.getUnsafe().callMethod("allocateMemory", sz);
		if(valr == null) {
			return 0;
		}
		return (long) ((Long) valr.one);
	}
	
	public static long zalloc(long sz) {
		Val.OneVal<Object> valr = Api.getUnsafe().callMethod("allocateMemory", sz);
		if(valr == null) {
			return 0;
		}
		long ptr = (long) ((Long) valr.one);
		Memory.memset(ptr, Byte.MIN_VALUE, sz);
		return ptr;
	}
	
	public static long realloc(long ptr, long nevsz) {
		Val.OneVal<Object> valr = Api.getUnsafe().callMethod("reallocateMemory", ptr, nevsz);
		if(valr == null) {
			return 0;
		}
		return (long) ((Long) valr.one);
	}
	
	public static void free(long ptr) {
		try {
			Api.getUnsafe().callMethod("freeMemory", ptr);
		} catch(Throwable t) {}
	}
	
	
	
	public static long getPointer(Object obj) {
		Object arr[] = new Object[] { obj };

		Val.OneVal<Object> valr = Api.getUnsafe().callMethod("arrayBaseOffset", Object[].class);
		if(valr == null) {
			return 0;
		}
		long base_offset = (long) ((Long) valr.one);
		
		if(Api.is64()) {
			valr = Api.getUnsafe().callMethod("getLong", arr, base_offset);
			if(valr != null) {
				return (long) ((Long) valr.one);
			}
		} else {
			valr = Api.getUnsafe().callMethod("getInt", arr, base_offset);
			if(valr != null) {
				return (int) ((Integer) valr.one);
			}
		}
		return 0;
	}
	
}
