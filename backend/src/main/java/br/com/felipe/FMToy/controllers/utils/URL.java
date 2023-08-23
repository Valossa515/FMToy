package br.com.felipe.FMToy.controllers.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class URL {
	
	public static String decodeParam(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	public static List<Long> decodeLongList(String s) {
		String[] vet = s.split(",");
		List<Long> list = new ArrayList<>();
		for (int i = 0; i < vet.length; i++) {
			list.add(Long.parseLong(vet[i]));
		}
		return list;
	}
	
	
}
