package net.onlyid.user_profile.location;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import androidx.fragment.app.Fragment;

import net.onlyid.common.MyAdapter;
import net.onlyid.databinding.FragmentCityBinding;
import net.onlyid.databinding.ItemLocationBinding;

import java.util.List;

public class CityFragment extends Fragment implements AdapterView.OnItemClickListener {
    static final String TAG = "CityFragment";

    FragmentCityBinding binding;
    List<String> cityList;

    BaseAdapter adapter = new MyAdapter(() -> cityList) {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemLocationBinding binding;
            if (convertView == null) {
                binding = ItemLocationBinding.inflate(getLayoutInflater());
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (ItemLocationBinding) convertView.getTag();
            }

            binding.textView.setText(cityList.get(position));

            return convertView;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cityList = getArguments().getStringArrayList("cityList");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCityBinding.inflate(inflater, container, false);

        binding.getRoot().setAdapter(adapter);
        binding.getRoot().setOnItemClickListener(this);

        return binding.getRoot();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        EditLocationActivity activity = (EditLocationActivity) getActivity();
        activity.submit(getArguments().getString("province"), cityList.get(position));
    }
}
