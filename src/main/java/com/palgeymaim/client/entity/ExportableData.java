package com.palgeymaim.client.entity;

import java.util.List;

public interface ExportableData {
	
	 List<String> headers();
	 List<List<String>> data();

}
