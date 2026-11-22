# Test Plan and Acceptance Criteria

## Automated foundation tests

- Android Gradle build succeeds.
- Unit tests pass.
- Editable DOCX ZIP entries and XML are valid.
- Arabic RTL properties and XML escaping are present.
- Visual DOCX includes page image entries and relationships.
- Visual DOCX document and relationship XML parse successfully.
- OCR engine dependency resolves and app compiles with native libraries.

## PDF input corpus

Prepare redistributable fixtures for:
- English selectable-text PDF.
- Arabic selectable-text PDF.
- Mixed Arabic/English PDF.
- Multi-page PDF with portrait and landscape pages.
- Image-only scanned Arabic PDF.
- Image-only scanned English PDF.
- Mixed text and image PDF.
- Tables, diagrams, and embedded images.
- Corrupt, empty, and password-protected PDFs.
- Large multi-page PDF.

Never commit private user documents.

## OCR validation

- Download English and Arabic language models on a real Android device.
- Confirm model files are stored in app-private storage.
- Enable airplane mode after model download and run OCR.
- Compare OCR text against labelled ground truth for Arabic, English, and mixed content.
- Record recognition accuracy, duration, and device model.
- Test rotated, skewed, low-contrast, and noisy scans.
- Confirm no PDF bytes are transmitted.

## Editable DOCX validation

- Extracted and OCR text is present in the correct page order.
- Arabic paragraphs use RTL properties.
- Mixed-script text and punctuation are manually inspected.
- Output opens in Microsoft Word or LibreOffice.
- Known limitations around layout, tables, and embedded images are recorded.

## Visual DOCX validation

- Every PDF page appears once and in order.
- Page images retain source aspect ratio.
- Portrait and landscape page cases are inspected.
- Output opens in Microsoft Word or LibreOffice.
- Large-file memory behavior is measured.

## Reliability and performance

- Corrupt and encrypted files show clear errors.
- Cancellation does not leave a falsely successful DOCX.
- Backgrounding the app does not corrupt output.
- Large PDFs do not cause unbounded memory use.
- Low storage and network failure during model download are handled.
- No document contents are uploaded.

## Release gates

- CI tests pass.
- Debug APK builds and is uploaded as an artifact.
- OCR works offline after models are downloaded.
- Both output modes are manually inspected in a compatible office application.
- Physical-device testing records Android version, device, PDF page count, file size, processing duration, and observed memory use.
- Known limitations remain visible until their corresponding implementation and tests are complete.

A successful CI build alone does not certify complete PDF-to-Word fidelity or production readiness.
