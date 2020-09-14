/*
 * Nextcloud Quicknotes Android client application.
 *
 * @copyright Copyright (c) 2020 Matias De lellis <mati86dl@gmail.com>
 *
 * @author Matias De lellis <mati86dl@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ar.delellis.quicknotes.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Note implements Serializable {
    @Expose
    @SerializedName("id") private int id;

    @Expose
    @SerializedName("title") private String title;

    @Expose
    @SerializedName("content") private String content;

    @Expose
    @SerializedName("ispinned") private boolean is_pinned;

    @Expose
    @SerializedName("color") private String color;

    @Expose
    @SerializedName("timestamp") private int timestamp;

    @Expose
    @SerializedName("shared_with") private List<Share> share_with;

    @Expose
    @SerializedName("is_shared") private boolean is_shared;

    @Expose
    @SerializedName("tags") private List<Tag> tags;

    @Expose
    @SerializedName("attachts") private List<Attachment> attachts;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean getIsPinned() {
        return is_pinned;
    }

    public void setIsPinned(boolean is_pinned) {
        this.is_pinned = is_pinned;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public List<Share> getShareWith() {
        return share_with;
    }

    public void setShareWith(List<Share> share) {
        this.share_with = share;
    }

    public boolean getIsShared() {
        return is_shared;
    }

    public void setIsShared(boolean is_shared) {
        this.is_shared = is_shared;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Attachment> getAttachts() {
        return attachts;
    }

    public void setAttachts(List<Attachment> attachts) {
        this.attachts = attachts;
    }

    public static Comparator<Note> ByTitleAZ = (note, t1) -> note.title.compareTo(t1.title);

    public static Comparator<Note> ByLastUpdated = (note, t1) -> t1.timestamp - note.timestamp;

    public static Comparator<Note> ByLastCreated = (note, t1) -> t1.id - note.id;

    public static Comparator<Note> ByPinned = (note, t1) -> {
        if (note.is_pinned && t1.is_pinned) {
            return 0;
        } else if (note.is_pinned) {
            return -1;
        } else if (t1.is_pinned) {
            return 1;
        }
        return 0;
    };

}
