package org.example.elearning_backend.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.example.elearning_backend.model.Course;
import org.example.elearning_backend.model.Enrollment;
import org.example.elearning_backend.model.User;
import org.example.elearning_backend.repository.CourseRepository;
import org.example.elearning_backend.repository.EnrollmentRepository;
import org.example.elearning_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CertificateService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    public byte[] generateCertificate(Long enrollmentId, Long userId) throws Exception {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        // Verify ownership
        if (!enrollment.getStudent().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to download this certificate");
        }

        // Check if course is completed
        if (!enrollment.getCompleted() && enrollment.getProgress() < 100) {
            throw new RuntimeException("Course not completed yet. Complete all materials first.");
        }

        Course course = enrollment.getCourse();
        User student = enrollment.getStudent();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);

        // Certificate Title
        Paragraph title = new Paragraph("CERTIFICATE OF COMPLETION")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(30)
                .setBold()
                .setMarginTop(100);
        document.add(title);

        // Subtitle
        Paragraph subtitle = new Paragraph("This certificate is proudly presented to")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(16)
                .setMarginTop(30);
        document.add(subtitle);

        // Student Name
        Paragraph studentName = new Paragraph(student.getFirstName() + " " + student.getLastName())
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(36)
                .setBold()
                .setFontColor(ColorConstants.BLUE)
                .setMarginTop(20);
        document.add(studentName);

        // Course completion text
        Paragraph completedText = new Paragraph("for successfully completing the course")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(16)
                .setMarginTop(30);
        document.add(completedText);

        // Course Name
        Paragraph courseName = new Paragraph(course.getTitle())
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(24)
                .setBold()
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginTop(20);
        document.add(courseName);

        // Date completed - Now using updatedAt field
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        String completedDate = enrollment.getUpdatedAt() != null ?
                enrollment.getUpdatedAt().format(formatter) :
                LocalDateTime.now().format(formatter);

        Paragraph dateText = new Paragraph("Completed on: " + completedDate)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12)
                .setMarginTop(40);
        document.add(dateText);

        // Signature line
        Paragraph signature = new Paragraph("_________________________")
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(60);
        document.add(signature);

        Paragraph signatureLabel = new Paragraph("Authorized Signature")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY);
        document.add(signatureLabel);

        // Certificate ID
        Paragraph certId = new Paragraph("Certificate ID: " + enrollmentId + "-" + System.currentTimeMillis())
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setMarginTop(40);
        document.add(certId);

        document.close();

        return outputStream.toByteArray();
    }
}