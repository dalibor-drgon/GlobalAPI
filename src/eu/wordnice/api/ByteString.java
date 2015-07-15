package eu.wordnice.api;

public class ByteString {
	
	public static boolean equals(byte[] str1, int str1_off, int str1_len,
			byte[] str2, int str2_off, int str2_len) {
		if(str1_len != str2_len) {
			return false;
		}
		int i1 = str1_off;
		int i2 = str2_off;
		for(; i1 < str1_len; i1++, i2++) {
			if(str1[i1] != str2[i2]) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean equalsIgnoreCase(byte[] str1, int str1_off, int str1_len,
			byte[] str2, int str2_off, int str2_len) {
		if(str1_len != str2_len) {
			return false;
		}
		int i1 = str1_off;
		int i2 = str2_off;
		for(; i1 < str1_len; i1++, i2++) {
			if(ByteChar.toLower(str1[i1]) != ByteChar.toLower(str2[i2])) {
				return false;
			}
		}
		return true;
	}
	
	
	
	public static int indexOf(byte[] str1, int str1_off, int str1_len,
			byte[] str2, int str2_off, int str2_len) {
		if(str1_len < str2_len) {
			return -1;
		}
		if(str1_len == str2_len) {
			return ByteString.equals(str1, str1_off, str1_len, str2, str2_off, str2_len)
					? str1_off : -1;
		}
		int i2 = str2_off;
		int maxi = str1_len - str2_len + str1_off;
		int i = str1_off;
		for(; i <= maxi; i++, i2++) {
			if(ByteString.equals(str1, i, str2_len, str2, i2, str2_len)) {
				return i;
			}
		}
		return -1;
	}
	
	public static Val.TwoVal<Integer, Integer> indexOf(byte[] str1, int str1_off, int str1_len,
			int minlen, Handler.FourVoidHandler<Val.TwoVal<Boolean, Integer>, byte[], Integer, Integer> handl) {
		if(str1_len < minlen) {
			return null;
		}
		Val.TwoVal<Boolean, Integer> check = new Val.TwoVal<Boolean, Integer>();
		int maxi = str1_len - minlen + str1_off;
		int i = str1_off;
		for(; i <= maxi; i++) {
			handl.handle(check, str1, i, (maxi + minlen - i));
			if(check.one) {
				return new Val.TwoVal<Integer, Integer>(i, check.two);
			}
		}
		return null;
	}
	
}
