# Test Plan and Acceptance Criteria

## Test corpus

Maintain a small redistributable local corpus containing:
- English PDF with headings and multiple paragraphs.
- Arabic PDF with right-to-left paragraphs and punctuation.
- Mixed Arabic/English PDF.
- Multi-page PDF with different page sizes or orientations.
- Selectable-text PDF with unusual spacing and columns.
- Image-only scanned Arabic PDF.
- Image-only scanned English PDF.
- Mixed page containing selectable text and scanned image regions.
- Tables with and without visible borders.
- PDF containing embedded images, diagrams, and charts.
- PDF containing mathematical notation.
- Empty, corrupt/truncated, and encrypted PDFs.
- Large multi-page PDF for memory and responsiveness checks.

Use generated fixtures or documents with redistribution permission. Never commit private user documents.

## Foundation tests

- Application compiles with the pinned Android Gradle Plugin and Gradle versions.
- Unit test suite passes.
- SAF picker returns a readable PDF URI.
- PDF page count is read locally.
- Invalid, encrypted, or unsupported PDFs show a useful error.
- Manifest does not declare Internet permission.

## PDF analysis tests

- Page count and page dimensions are correct.
- Text extraction retains Unicode and page order.
- Positioned text retains coordinates and font metadata where available.
- Embedded images are extracted without accidental duplication.
- Image-only pages are classified as requiring OCR.
- Mixed pages retain both native text and image regions.
- Malformed input fails safely without an empty-success result.

## OCR tests

- Arabic, English, and mixed-language recognition.
- Recognition accuracy measured against labelled ground truth.
- Reading order and bounding boxes are retained.
- Confidence values are surfaced for uncertain regions.
- Rotation, skew, low contrast, and noisy scans are included.
- OCR is executed locally in airplane mode.

## DOCX tests

- ZIP contains [Content_Types].xml, _rels/.rels, word/document.xml, and all referenced parts.
- XML is well-formed and relationships resolve.
- XML special characters are escaped.
- Arabic and English Unicode survive round-trip.
- RTL paragraphs and mixed-script runs are represented correctly.
- Images and table relationships resolve.
- Page count/order is retained.
- Generated DOCX opens in Microsoft Word or a compatible office application.

## Output-mode tests

- Editable mode produces native editable text and tables where supported.
- Visual mode includes every source page in order at the selected render resolution.
- Visual mode preserves page aspect ratio and orientation.
- Hybrid fallback does not omit unrecognized regions.
- No mode reports success when output validation fails.

## Performance and reliability

- Large PDFs do not cause unbounded bitmap retention.
- Conversion cancellation removes incomplete temporary output.
- App backgrounding does not corrupt conversion state.
- Low-storage conditions produce a clear error.
- Airplane-mode operation succeeds after dependencies are installed.
- No document contents are transmitted.

## Manual device verification

- Open output DOCX in a compatible Android office app.
- Inspect Arabic RTL, English LTR, punctuation, numbers, and mixed-script lines.
- Compare tables, images, and page layouts against source.
- Test on a physical Android device and record model, Android version, file size, page count, duration, and observed memory behavior.

## Acceptance gates

A workflow file existing is not evidence that tests passed. Record actual CI run results and manual inspection. APK distribution is blocked until all required gates pass.

## Expected outcomes

- Image-only pages must be routed to OCR; they must not silently produce a blank successful DOCX.
- Encrypted PDFs require an accepted password or return a clear unsupported/password-required error. Password bypass is not supported.
- When exact semantic reconstruction is unreliable, preserve the visual region as an image and report the fallback.
