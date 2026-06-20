package android.print;

import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PageRange;

public class PdfHelper {
    public interface Callback {
        void onSuccess();
        void onFailure();
    }

    public static void savePdfFromAdapter(
            final PrintDocumentAdapter adapter,
            final PrintAttributes attributes,
            final ParcelFileDescriptor pfd,
            final Callback callback
    ) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    adapter.onLayout(null, attributes, null, new PrintDocumentAdapter.LayoutResultCallback() {
                        @Override
                        public void onLayoutFinished(PrintDocumentInfo info, boolean changed) {
                            try {
                                adapter.onWrite(new PageRange[]{PageRange.ALL_PAGES}, pfd, null, new PrintDocumentAdapter.WriteResultCallback() {
                                    @Override
                                    public void onWriteFinished(PageRange[] pages) {
                                        callback.onSuccess();
                                    }

                                    @Override
                                    public void onWriteFailed(CharSequence error) {
                                        callback.onFailure();
                                    }

                                    @Override
                                    public void onWriteCancelled() {
                                        callback.onFailure();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                callback.onFailure();
                            }
                        }

                        @Override
                        public void onLayoutFailed(CharSequence error) {
                            callback.onFailure();
                        }

                        @Override
                        public void onLayoutCancelled() {
                            callback.onFailure();
                        }
                    }, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailure();
                }
            }
        });
    }
}
