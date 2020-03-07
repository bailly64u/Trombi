package android.print;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import androidx.core.content.FileProvider;

import com.ufrst.app.trombi.util.Logger;

import java.io.File;
import java.io.IOException;

// Singleton gérant la création de PDF en vue d'un partage. Le PDF ne peut pas être modifié
// par l'utilisateur
public class PDFPrinter {

    private File pdf;

    private static PDFPrinter instance = null;

    public static synchronized PDFPrinter getInstance(){
        if(instance == null)
            instance = new PDFPrinter();

        return instance;
    }

    private PDFPrinter(){
    }

    public void print(PrintDocumentAdapter printAdapter, final String filepath){
        printAdapter.onLayout(null,
                getDefaultAttributes(),
                null,
                new PrintDocumentAdapter.LayoutResultCallback() {
                    @Override
                    public void onLayoutFinished(PrintDocumentInfo info, boolean changed){
                        printAdapter.onWrite(new PageRange[]{PageRange.ALL_PAGES},
                                getOutputFileDescriptor(filepath),
                                new CancellationSignal(),
                                new PrintDocumentAdapter.WriteResultCallback() {
                                    @Override
                                    public void onWriteFinished(PageRange[] pages){

                                        super.onWriteFinished(pages);
                                    }
                                });
                    }
                }, null);
    }

    private PrintAttributes getDefaultAttributes(){
        return new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .setResolution(new PrintAttributes.Resolution("Res",
                        "Resolution_id",
                        600,
                        600))
                .build();
    }

    private ParcelFileDescriptor getOutputFileDescriptor(String filepath){
        pdf = new File(filepath);

        try{
            pdf.createNewFile();

            return ParcelFileDescriptor.open(pdf,
                    ParcelFileDescriptor.MODE_TRUNCATE |
                            ParcelFileDescriptor.MODE_READ_WRITE);
        } catch(IOException e){
            Logger.handleException(e);
            return null;
        }
    }

    // Retourne un Intent pour visionner / envoyer le PDF qui peut être déclenché depuis une activité
    public Intent makeIntent(Context context){
        String authority = context.getApplicationContext().getPackageName() + ".provider";
        Uri pdfUri = FileProvider.getUriForFile(context, authority, pdf);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, pdfUri);

        return intent;
    }
}
