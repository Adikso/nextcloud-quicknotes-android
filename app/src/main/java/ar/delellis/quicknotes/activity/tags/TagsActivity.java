/*
 * Nextcloud Quicknotes Android client application.
 *
 * @copyright Copyright (c) 2020 Matias De lellis <mati86dl@gmail.com>
 *
 * @author Matias De lellis <mati86dl@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ar.delellis.quicknotes.activity.tags;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import ar.delellis.quicknotes.R;
import ar.delellis.quicknotes.model.Tag;
import ar.delellis.quicknotes.shared.TagSelectionAdapter;


public class TagsActivity extends AppCompatActivity {

    TagSelectionAdapter tagsAdapter;
    RecyclerView tagsRecyclerView;

    List<Tag> tags;
    List<Tag> tagSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        tagsAdapter = new TagSelectionAdapter();
        tagsRecyclerView = findViewById(R.id.recycler_tags_view);

        Intent intent = getIntent();
        tags = (List<Tag>) Objects.requireNonNull(intent.getSerializableExtra("tags"));
        tagSelection = (List<Tag>) Objects.requireNonNull(intent.getSerializableExtra("tagSelection"));

        tagsAdapter.setTags(tags);
        tagsAdapter.setTagSelection(tagSelection);

        tagsAdapter.notifyDataSetChanged();
        tagsRecyclerView.setAdapter(tagsAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent();
        intent.putExtra("tagSelection", (Serializable) tagsAdapter.getTagSelection());
        setResult(RESULT_OK, intent);
        finish();

        return true;
    }

}