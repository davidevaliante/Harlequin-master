package com.finder.harlequinapp.valiante.harlequin;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import es.dmoral.toasty.Toasty;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.SharingHelper;
import io.branch.referral.util.LinkProperties;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by akain on 05/08/2017.
 */

public class CustomShareDialog extends DialogFragment {

    protected RelativeLayout facebookShare,telegramShare,whatsappShare,copyLink;
    private String eTitle,eDescription,eImage,eId;
    private ShareDialog shareDialog;
    private ProgressDialog progressDialog;

    public  CustomShareDialog(){
        //empty constructor
    }

    public static CustomShareDialog newInstance(String title, String description, String image,String event_id){
        CustomShareDialog newFrag = new CustomShareDialog();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("TITLE", title);
        args.putString("DESCRIPTION", description);
        args.putString("IMAGE", image);
        args.putString("ID",event_id);
        newFrag.setArguments(args);


        return newFrag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.share_dialog_layout,container,false);

        facebookShare = (RelativeLayout)rootView.findViewById(R.id.shareWithFacebook);
        telegramShare = (RelativeLayout)rootView.findViewById(R.id.shareWithTelegram);
        whatsappShare = (RelativeLayout)rootView.findViewById(R.id.shareWithWhatsapp);
        copyLink = (RelativeLayout)rootView.findViewById(R.id.share_copy);

        eTitle = getArguments().getString("TITLE");
        eDescription = getArguments().getString("DESCRIPTION");
        eImage = getArguments().getString("IMAGE");
        eId = getArguments().getString("ID");
        shareDialog = new ShareDialog(getActivity());

        progressDialog = UbiquoUtils.defaultProgressBar("Attendere prego",getActivity());

        facebookShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                shareElementWithBranchOnFacebook(eTitle,eDescription,eImage,eId,"facebook");
            }
        });

        whatsappShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                shareElementWithBranchOnWhatsapp(eTitle,eDescription,eImage,eId,"whatsapp");
            }
        });

        telegramShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                shareElementWithBranchOnTelegram(eTitle,eDescription,eImage,eId,"telegram");
            }
        });

        copyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                buildLinkWithBranchAndCopyItInClipboard(eTitle,eDescription,eImage,eId,"copied_link");
            }
        });

        return rootView;
    }

    protected void shareElementWithBranchOnFacebook(String title, String description, String image, String eventId, String channel) {
        final BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                .setCanonicalIdentifier(eventId)
                .setTitle(title)
                .setContentDescription(description)
                .setContentImageUrl(image)
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)

                .addContentMetadata("EVENT_ID", eventId);

        LinkProperties linkProperties = new LinkProperties()
                .setCampaign("Eventi")
                .setChannel(channel)
                .setFeature("sharing")
                .addControlParameter("$desktop_url", "https://play.google.com/store/apps/details?id=com.finder.harlequinapp.valiante.harlequin")
                .addControlParameter("$ios_url", "https://play.google.com/store/apps/details?id=com.finder.harlequinapp.valiante.harlequin");


        branchUniversalObject.generateShortUrl(getActivity(), linkProperties, new Branch.BranchLinkCreateListener() {
            @Override
            public void onLinkCreate(String url, BranchError error) {
                if (error == null) {
                    Log.i("MyApp", "got my Branch link to share: " + url);
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentUrl(Uri.parse(url))
                            .build();
                    shareDialog.show(linkContent);
                    progressDialog.dismiss();
                    dismiss();




                } else {
                    Toast.makeText(getActivity(), "Errore, riprova", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    dismiss();
                }
            }
        });
    }

    protected void shareElementWithBranchOnTelegram(String title, String description, String image, String eventId, String channel) {
        final BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                .setCanonicalIdentifier(eventId)
                .setTitle(title)
                .setContentDescription(description)
                .setContentImageUrl(image)
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)

                .addContentMetadata("EVENT_ID", eventId);

        LinkProperties linkProperties = new LinkProperties()
                .setCampaign("Eventi")
                .setChannel(channel)
                .setFeature("sharing")
                .addControlParameter("$desktop_url", "https://play.google.com/store/apps/details?id=com.finder.harlequinapp.valiante.harlequin")
                .addControlParameter("$ios_url", "https://play.google.com/store/apps/details?id=com.finder.harlequinapp.valiante.harlequin");


        branchUniversalObject.generateShortUrl(getActivity(), linkProperties, new Branch.BranchLinkCreateListener() {
            @Override
            public void onLinkCreate(String url, BranchError error) {
                if (error == null) {
                    Log.i("MyApp", "got my Branch link to share: " + url);
                   /* ShareLinkContent linkContent = new ShareLinkContent.Builder()

                            .setContentUrl(Uri.parse(url))

                            .build();
                    shareDialog.show(linkContent);*/

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage("org.telegram.messenger");

                    startActivity(sendIntent);
                    progressDialog.dismiss();
                    dismiss();

                } else {
                    Toast.makeText(getActivity(), "Errore, riprova", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    dismiss();
                }
            }
        });
    }

    protected void shareElementWithBranchOnWhatsapp(String title, String description, String image, String eventId, String channel) {
        final BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                .setCanonicalIdentifier(eventId)
                .setTitle(title)
                .setContentDescription(description)
                .setContentImageUrl(image)
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)

                .addContentMetadata("EVENT_ID", eventId);

        LinkProperties linkProperties = new LinkProperties()
                .setCampaign("Eventi")
                .setChannel(channel)
                .setFeature("sharing")
                .addControlParameter("$desktop_url", "https://play.google.com/store/apps/details?id=com.finder.harlequinapp.valiante.harlequin")
                .addControlParameter("$ios_url", "https://play.google.com/store/apps/details?id=com.finder.harlequinapp.valiante.harlequin");


        branchUniversalObject.generateShortUrl(getActivity(), linkProperties, new Branch.BranchLinkCreateListener() {
            @Override
            public void onLinkCreate(String url, BranchError error) {
                if (error == null) {
                    Log.i("MyApp", "got my Branch link to share: " + url);
                   /* ShareLinkContent linkContent = new ShareLinkContent.Builder()

                            .setContentUrl(Uri.parse(url))

                            .build();
                    shareDialog.show(linkContent);*/

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage("com.whatsapp");

                    startActivity(sendIntent);
                    progressDialog.dismiss();
                    dismiss();


                } else {
                    Toast.makeText(getActivity(), "Errore, riprova", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    dismiss();
                }
            }
        });
    }

    protected void buildLinkWithBranchAndCopyItInClipboard(String title, String description, String image, String eventId, String channel) {
        final BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                .setCanonicalIdentifier(eventId)
                .setTitle(title)
                .setContentDescription(description)
                .setContentImageUrl(image)
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)

                .addContentMetadata("EVENT_ID", eventId);

        LinkProperties linkProperties = new LinkProperties()
                .setCampaign("Eventi")
                .setChannel(channel)
                .setFeature("sharing")
                .addControlParameter("$desktop_url", "https://play.google.com/store/apps/details?id=com.finder.harlequinapp.valiante.harlequin")
                .addControlParameter("$ios_url", "https://play.google.com/store/apps/details?id=com.finder.harlequinapp.valiante.harlequin");


        branchUniversalObject.generateShortUrl(getActivity(), linkProperties, new Branch.BranchLinkCreateListener() {
            @Override
            public void onLinkCreate(String url, BranchError error) {
                if (error == null) {
                    Log.i("MyApp", "got my Branch link to share: " + url);

                    ClipboardManager clipboard = (ClipboardManager)getActivity().getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(eId, url);
                    clipboard.setPrimaryClip(clip);
                    Toasty.info(getActivity(),"Link copiato negli appunti", Toast.LENGTH_SHORT,true).show();
                    progressDialog.dismiss();
                    dismiss();

                } else {
                    Toast.makeText(getActivity(), "Errore, riprova", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    dismiss();
                }
            }
        });
    }




}
