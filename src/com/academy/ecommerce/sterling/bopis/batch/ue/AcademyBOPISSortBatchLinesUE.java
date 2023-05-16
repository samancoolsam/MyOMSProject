package com.academy.ecommerce.sterling.bopis.batch.ue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.w3c.dom.Document;

import com.academy.util.constants.AcademyConstants;
import com.yantra.pca.ycd.japi.ue.YCDSortBatchLinesUE;
import com.yantra.util.YFCUtils;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

public class AcademyBOPISSortBatchLinesUE implements YCDSortBatchLinesUE {

	YFCLogCategory logger = YFCLogCategory.instance(AcademyBOPISSortBatchLinesUE.class.getName());
	static String strDescFlag = AcademyConstants.STR_NO;

	public Document sortBatchLines(YFSEnvironment arg0, Document inDoc) throws YFSUserExitException {
		// TODO Auto-generated method stub
		YFCDocument getSortedBatchListOutPutYFCDoc = YFCDocument.getDocumentFor(inDoc);

		if (logger.isVerboseEnabled()) {
			logger.verbose("***************** BatchLines in a batch before sorting: ********************");
			logger.verbose(
					"AcademyBOPISSortBatchLinesUE:sortBatchLines:Input " + getSortedBatchListOutPutYFCDoc.toString());
		}

		// fetch the batch lines and store them in an array list
		YFCElement eleStoreBatchOutput = getSortedBatchListOutPutYFCDoc.getDocumentElement();
		YFCElement eleStoreBatchLines = eleStoreBatchOutput.getChildElement(AcademyConstants.ELE_STORE_BATCH_LINES);

		// Array list to store batch lines
		List<YFCElement> batchList = new ArrayList<YFCElement>();
		// iterate over the batch line and store them in array list
		final YFCNodeList<YFCElement> nlBatchList = eleStoreBatchLines
				.getElementsByTagName(AcademyConstants.ELE_STORE_BATCH_LINE);
		for (YFCElement eleBatchLine : nlBatchList) {
			batchList.add(eleBatchLine);
		}

		Collections.sort(batchList, new SortBatchList());

		YFCElement eleBatchLinesNew = eleStoreBatchOutput.createChild(AcademyConstants.ELE_STORE_BATCH_LINES);
		String sTotalNoOfRecords = eleStoreBatchLines.getAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS);
		eleStoreBatchOutput.removeChild(eleStoreBatchLines);
		int length = batchList.size();
		for (int i = 0; i < length; i++) {
			YFCElement eleBatchLine = batchList.get(i);
			eleBatchLinesNew.importNode(eleBatchLine);
		}

		eleBatchLinesNew.setAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS, sTotalNoOfRecords);
		if (logger.isVerboseEnabled()) {
			logger.verbose("***************** BatchLines in a batch after sorting: ********************");
			logger.verbose(
					"AcademyBOPISSortBatchLinesUE:sortBatchLines:Output: " + getSortedBatchListOutPutYFCDoc.toString());
		}
		return getSortedBatchListOutPutYFCDoc.getDocument();
	}

	@SuppressWarnings("serial")
	static class SortBatchList implements Comparator<YFCElement>, Serializable {

		public int compare(YFCElement elem1, YFCElement elem2) {
			String value1 = AcademyConstants.STR_EMPTY_STRING;
			String value2 = AcademyConstants.STR_EMPTY_STRING;
			int value = 0;

			// fetch the elementDetail from the batch line element
			YFCElement eleItemDetailselem1 = elem1.getChildElement(AcademyConstants.ELE_ITEM_DETAILS);
			YFCElement eleItemDetailselem2 = elem2.getChildElement(AcademyConstants.ELE_ITEM_DETAILS);

			if (!YFCCommon.isVoid(eleItemDetailselem1) && !YFCCommon.isVoid(eleItemDetailselem2)) {

				YFCElement eleExtn1 = eleItemDetailselem1.getChildElement(AcademyConstants.ELE_EXTN);
				YFCElement eleExtn2 = eleItemDetailselem2.getChildElement(AcademyConstants.ELE_EXTN);

				if (!YFCCommon.isVoid(eleExtn1) && !YFCCommon.isVoid(eleExtn2)) {
					value1 = eleExtn1.getAttribute(AcademyConstants.ATTR_EXTNDEPARTMENT).toUpperCase();
					value2 = eleExtn2.getAttribute(AcademyConstants.ATTR_EXTNDEPARTMENT).toUpperCase();
					
					if (!YFCCommon.isVoid(value1) && !YFCCommon.isVoid(value2)) {
						
						if (YFCUtils.equals(strDescFlag, AcademyConstants.STR_YES)) {
							value = value2.compareTo(value1);
						} else {
							value = value1.compareTo(value2);
						}
					}
				}
			}
			
			return value;
		}
	}
}
