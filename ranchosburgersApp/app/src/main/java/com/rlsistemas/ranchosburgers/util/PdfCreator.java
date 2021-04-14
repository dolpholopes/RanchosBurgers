package com.rlsistemas.ranchosburgers.util;


import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;


public class PdfCreator extends PdfPageEventHelper {


    //variaveis para criar numeracao de paginas
    Phrase[] header = new Phrase[2];
    int pagenumber;

        //------------------------------ CRIA NUMERACAO NO PDF------------------------------------------------------------------

        public void onOpenDocument(PdfWriter writer, Document document) {
            header[0] = new Phrase("Movie history");
        }


        public void onChapter(PdfWriter writer, Document document,
                              float paragraphPosition, Paragraph title) {
            header[1] = new Phrase(title.getContent());
            pagenumber = 1;
        }


        public void onStartPage(PdfWriter writer, Document document) {
            pagenumber++;
        }


        public void onEndPage(PdfWriter writer, Document document)
        {


            Rectangle rect1 = writer.getBoxSize("box");
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_RIGHT, new Phrase(""),
                    rect1.getRight(), rect1.getTop(), 0);
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_RIGHT, new Phrase(String.format("%d", pagenumber)),
                    (rect1.getRight() + rect1.getRight()) / 2, rect1.getBottom() - 18, 0);


        }


}
