package net.onlyid.user_info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import androidx.fragment.app.Fragment;

import net.onlyid.databinding.FragmentCityBinding;
import net.onlyid.databinding.ItemLocationBinding;

import java.util.List;

public class CityFragment extends Fragment implements AdapterView.OnItemClickListener {
    static final String TAG = CityFragment.class.getSimpleName();

    FragmentCityBinding binding;
    List<String> cityList;

    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return cityList.size();
        }

        @Override
        public Object getItem(int position) {
            return cityList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

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

            binding.arrowRight.getRoot().setVisibility(View.INVISIBLE);
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
        activity.submit(getArguments().getString("province") + " " + cityList.get(position));
    }
}