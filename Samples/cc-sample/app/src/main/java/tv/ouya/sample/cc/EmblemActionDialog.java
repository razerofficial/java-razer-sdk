package tv.ouya.sample.cc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.razerzone.store.sdk.ErrorCodes;
import com.razerzone.store.sdk.content.GameMod;
import com.razerzone.store.sdk.content.GameModManager;

import java.util.ArrayList;
import java.util.Set;

public class EmblemActionDialog {

    private static final String TAG = EmblemActionDialog.class.getSimpleName();

    public static void showDialog(final Context context, final GameMod mod, final ActionCompleteListener listener) {
        final ArrayList<EmblemAction> choices = new ArrayList<EmblemAction>();

        choices.add(EmblemAction.INFO);
        if(mod.isOwnedByCurrentUser()) {
            if(mod.isInstalled()) {
                choices.add(EmblemAction.EDIT);
            }
            choices.add(EmblemAction.PUBLISH);
            if(mod.isPublished()) {
                choices.add(EmblemAction.UNPUBLISH);
            }
        }
        if(mod.isInstalled()) {
            if(mod.hasUpdate()) {
                choices.add(EmblemAction.UPDATE);
            }
            choices.add(EmblemAction.DELETE);
        } else {
            choices.add(EmblemAction.DOWNLOAD);
        }
        if(mod.isPublished()) {
            choices.add(EmblemAction.RATE);
            choices.add(EmblemAction.FLAG);
        }
        choices.add(EmblemAction.CANCEL);

        final ArrayAdapter<EmblemAction> adapter = new ArrayAdapter<EmblemAction>(context, android.R.layout.simple_selectable_list_item, choices);

        new AlertDialog.Builder(context)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EmblemAction action = adapter.getItem(which);
                        switch (action) {
                            case INFO:
                                showInfoDialog(context, mod);
                                break;
                            case EDIT:
                                Intent editIntent = new Intent(context, EditEmblemActivity.class);
                                editIntent.putExtra(EditEmblemActivity.EXTRA_MOD_UUID, mod.getUUID());
                                context.startActivity(editIntent);
                                break;
                            case PUBLISH:
                                mod.publish(new GameModManager.PublishListener() {
                                    @Override
                                    public void onSuccess(GameMod updatedMod) {
                                        Toast.makeText(context, "Publish successful!", Toast.LENGTH_SHORT).show();
                                        listener.onComplete(updatedMod);
                                    }

                                    @Override
                                    public void onError(GameMod mod, int errorCode, String errorReason, Bundle errorDetails) {
                                        if (errorCode == ErrorCodes.FIELD_VALIDATIONS_FAILED) {
                                            if (errorDetails != null) {
                                                if (errorDetails.containsKey("title")) {
                                                    Toast.makeText(context, "Invalid title: " + errorDetails.getStringArray("title")[0], Toast.LENGTH_SHORT).show();
                                                } else {
                                                    final StringBuilder fields = new StringBuilder();
                                                    final Set<String> keys = errorDetails.keySet();
                                                    for (String key : keys) {
                                                        fields.append(key).append(", ");
                                                    }
                                                    Toast.makeText(context, "Invalid fields:\n" + fields, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "Publish failed! (" + errorCode + ")\n" + errorReason, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                break;
                            case UNPUBLISH:
                                mod.unpublish(new GameModManager.UnpublishListener() {
                                    @Override
                                    public void onSuccess(GameMod updatedMod) {
                                        Toast.makeText(context, "Unpublish successful!", Toast.LENGTH_SHORT).show();
                                        listener.onComplete(updatedMod);
                                    }

                                    @Override
                                    public void onError(GameMod mod, int errorCode, String errorReason) {
                                        Toast.makeText(context, "Unpublish failed! (" + errorCode + ")\n" + errorReason, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            case DOWNLOAD:
                            case UPDATE:
                                mod.download(new GameModManager.DownloadListener() {
                                    @Override
                                    public void onComplete(GameMod updatedMod) {
                                        Toast.makeText(context, "Download successful!", Toast.LENGTH_SHORT).show();
                                        listener.onComplete(updatedMod);
                                    }

                                    @Override
                                    public void onProgress(GameMod mod, int percentComplete) {
                                    }

                                    @Override
                                    public void onFailed(GameMod mod) {
                                        Toast.makeText(context, "Download failed!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            case DELETE:
                                mod.delete(new GameModManager.DeleteListener() {
                                    @Override
                                    public void onDeleted(GameMod updatedMod) {
                                        Toast.makeText(context, "Delete successful!", Toast.LENGTH_SHORT).show();
                                        listener.onComplete(updatedMod);
                                    }

                                    @Override
                                    public void onDeleteFailed(GameMod mod, int code, String reason) {
                                        Toast.makeText(context, "Delete failed! (" + code + ")\n" + reason, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            case RATE:
                                mod.rate();
                                break;
                            case FLAG:
                                mod.flag();
                            default:
                            case CANCEL:
                                break;
                        }
                    }
                })
                .show();
    }

    private static void showInfoDialog(Context context, GameMod mod) {
        String info = "Title: "+mod.getTitle() + "\n";
        info += "Author: " + mod.getAuthor() + "\n";
        info += "Description: " + mod.getDescription() + "\n";
        info += "Installed Version: " + mod.getInstalledRevision() + "\n";
        info += "Latest Version: " + mod.getLatestRevision() + "\n";
        info += "Rating: " + mod.getRatingAverage() + " (" + mod.getRatingCount() + ")" + "\n";
        if(mod.getUserRating() != null) {
            info += "User Rating: " + mod.getUserRating() + "\n";
        } else {
            info += "User has not rated this mod" + "\n";
        }

        new AlertDialog.Builder(context).setMessage(info).setPositiveButton(android.R.string.ok, null).show();
    }

    private static enum EmblemAction {
        INFO("Info"),
        EDIT("Edit"),
        PUBLISH("Publish"),
        UNPUBLISH("Unpublish"),
        DOWNLOAD("Download"),
        UPDATE("Update"),
        DELETE("Delete"),
        RATE("Rate"),
        FLAG("Flag"),
        CANCEL("Cancel");
        public final String label;
        EmblemAction(String label) {
            this.label = label;
        }
        @Override
        public String toString() {
            return label;
        }
    }

    public static interface ActionCompleteListener {
        public void onComplete(GameMod updatedMod);
    }
}
