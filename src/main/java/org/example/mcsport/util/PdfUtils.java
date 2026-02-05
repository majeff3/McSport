package org.example.mcsport.util;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.font.FontProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;

@Service
public class PdfUtils {

    @Autowired
    private TemplateEngine templateEngine;

    public void createSimplePdfToStream(OutputStream outputStream, String text) throws IOException {
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        PdfFont chineseFont = PdfFontFactory.createFont("/fonts/STSong.ttf", PdfEncodings.IDENTITY_H);
        Document document = new Document(pdfDoc);
        document.add(new Paragraph(text).setFont(chineseFont));
        document.close();
    }

    public void generatePdfFromThymeleaf(OutputStream outputStream, String templateName, Map<String, Object> data) throws IOException {
        // 1. 使用 Thymeleaf 处理模板
        Context context = new Context();
        context.setVariables(data);
        // Process the template into HTML string
        String htmlContent = templateEngine.process(templateName, context);
        // 2. 配置 PDF 转换属性 (主要为了支持中文字体)
        ConverterProperties converterProperties = new ConverterProperties();
        // 创建字体提供者
        FontProvider fontProvider = new FontProvider();
        try {
            PdfFont chineseFont = PdfFontFactory.createFont("/fonts/STSong.ttf", PdfEncodings.IDENTITY_H);
            fontProvider.addFont(chineseFont.getFontProgram());
            // 设置字体提供者，这样 HTML 转换时才能识别中文字符
            converterProperties.setFontProvider(fontProvider);
        } catch (Exception e) {
            System.err.println("警告：无法加载中文字体，生成的 PDF 可能无法显示中文。 " + e.getMessage());
        }
        // 3. 将 HTML 转换为 PDF 并写入流
        HtmlConverter.convertToPdf(htmlContent, outputStream, converterProperties);
    }

}
