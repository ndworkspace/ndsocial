DecodeService decodeService = new DecodeServiceBase(new PdfContext());
decodeService.setTargetSize(documentView.getWidth(), documentView.getHeight());
decodeService.open(fileName);
RectF pageSliceBounds = new RectF(0, 0, 1.0f, 1.0f)
decodeService.decodePage(this, page.index, new DecodeService.DecodeCallback() {
	public void decodeComplete(final Bitmap bitmap) {
                documentView.post(new Runnable() {
                    public void run() { }
		}
	}
}, 1.0f, pageSliceBounds);
		