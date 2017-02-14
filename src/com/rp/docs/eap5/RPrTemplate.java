package com.rp.docs.eap5;

import java.math.BigInteger;

import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Color;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STHint;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;

public class RPrTemplate {
	public RPr titleRPr;
	public RPr subTitleRPr;
	public RPr boldRPr;
	public RPr regularRPr;
	public RPr tableHeaderRPr;
	public RPr tableContentRPr;
	public RPr smallRPr;
	
	public RPrTemplate(){
		
	}
	
	public RPrTemplate(ObjectFactory factory){
		this.titleRPr = getRPr(factory, "나눔고딕", "000000", "36", STHint.EAST_ASIA, true, false, false, false);
		this.subTitleRPr = getRPr(factory, "나눔고딕", "000000", "22", STHint.EAST_ASIA, true, false, false, false);
		this.boldRPr = getRPr(factory, "나눔고딕", "000000", "22", STHint.EAST_ASIA, true, false, false, false);
		this.regularRPr = getRPr(factory, "나눔고딕", "000000", "22", STHint.EAST_ASIA, false, false, false, false);
		this.tableHeaderRPr = getRPr(factory, "나눔고딕", "000000", "14", STHint.EAST_ASIA, true, false, false, false);
		this.tableContentRPr = getRPr(factory, "나눔고딕", "000000", "14", STHint.EAST_ASIA, false, false, false, false);
		this.smallRPr = getRPr(factory, "나눔고딕", "000000", "5", STHint.EAST_ASIA, false, false, false, false);
	}
	
	/**
	 * 글꼴 생성
	 * 
	 * @param isBlod
	 *            진하게
	 * @param isUnderLine
	 *            밑줄
	 * @param isItalic
	 *            이탤릭
	 * @param isStrike
	 *            취소선
	 */
	public RPr getRPr(ObjectFactory factory, 
						String fontFamily,
						String colorVal, 
						String fontSize, 
						STHint sTHint, 
						boolean isBlod,
						boolean isUnderLine, 
						boolean isItalic, 
						boolean isStrike
						) {
		RPr rPr = factory.createRPr();
		RFonts rf = new RFonts();
		rf.setHint(sTHint);
		rf.setAscii(fontFamily);
		rf.setHAnsi(fontFamily);
		rPr.setRFonts(rf);

		BooleanDefaultTrue bdt = factory.createBooleanDefaultTrue();
		rPr.setBCs(bdt);
		if (isBlod) {
			rPr.setB(bdt);
		}
		if (isItalic) {
			rPr.setI(bdt);
		}
		if (isStrike) {
			rPr.setStrike(bdt);
		}
		if (isUnderLine) {
			U underline = new U();
			underline.setVal(UnderlineEnumeration.SINGLE);
			rPr.setU(underline);
		}

		Color color = new Color();
		color.setVal(colorVal);
		rPr.setColor(color);

		HpsMeasure sz = new HpsMeasure();
		//sz.setVal(new BigInteger(fontSize));
		sz.setVal(BigInteger.valueOf(Integer.parseInt(fontSize)));
		rPr.setSz(sz);
		rPr.setSzCs(sz);
		return rPr;
	}
}
