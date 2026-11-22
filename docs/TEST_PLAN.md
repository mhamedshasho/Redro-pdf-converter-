# Test Plan and Acceptance Criteria

## Test corpus
Create and keep a small redistributable local test corpus with:
- English PDF containing headings and multiple paragraphs.
- Arabic PDF containing right-to-left paragraphs and punctuation.
- Mixed Arabic/English PDF.
- Multi-page PDF with page boundaries.
- Selectable-text PDF with unusual spacing.
- Image-only scanned PDF.
- Empty PDF.
- Corrupt/truncated PDF.
- Encrypted/password-protected PDF.
- Large multi-page PDF for memory and responsiveness checks.

Use generated fixtures or documents with permission to redistribute. Never commit private user documents.

## Unit tests
- Text normalization removes invalid control characters without joining unrelated words.
- Empty lines and paragraph boundaries are deterministic.
- Arabic text is detected and marked RTL.
- English text remains LTR.
- Mixed-script lines retain all characters.
- Page order is retained.
- DOCX ZIP contains [Content_Types].xml, _rels/.rels, and word/document.xml.
- DOCX XML is well-formed and contains extracted text.
- XML special characters are escaped.
- Unicode Arabic and English survive write/read round-trip.

## Instrumentation tests
- PDF can be selected using the system document picker.
- Output destination can be selected.
- Successful conversion produces a readable DOCX.
- Unsupported or empty input shows a useful error.
- Cancellation does not leave a partial output.
- Rotation/backgrounding does not corrupt conversion state.

## Manual device tests
- Open DOCX in a compatible Android office app.
- Confirm Arabic paragraphs are RTL/right-aligned where supported.
- Confirm English paragraphs remain LTR.
- Inspect punctuation, numbers, and mixed Arabic/English strings.
- Test multi-page and large files on a physical phone.
- Verify airplane-mode conversion after dependencies are installed.
- Confirm no PDF content is uploaded or transmitted.

## Acceptance gates
A test is not passed merely because a workflow file exists. Record actual command output and CI run results. APK distribution is blocked until automated checks pass and manual output inspection is recorded.

## Expected outcomes
- Image-only scanned PDFs report that no selectable text was found; they must not produce a blank DOCX reported as successful.
- Encrypted PDFs return a clear password-required/unsupported error.
- Layout-heavy PDFs may lose columns, tables, images, and exact positioning in the first release; this limitation must be visible to users.
