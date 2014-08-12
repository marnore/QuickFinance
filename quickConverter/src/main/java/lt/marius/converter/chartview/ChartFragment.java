package lt.marius.converter.chartview;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import lt.marius.charts.Axis;
import lt.marius.charts.Chart;
import lt.marius.charts.CombinedSeries2D;
import lt.marius.charts.PieChart;
import lt.marius.charts.Series2D;
import lt.marius.charts.SeriesPie;
import lt.marius.charts.XAxis;
import lt.marius.charts.YAxis;
import lt.marius.converter.R;
import lt.marius.converter.curview.Currency;
import lt.marius.converter.data.CurrencyProvider.IsoCode;
import lt.marius.converter.settings.SettingsProvider;
import lt.marius.converter.settings.SettingsProvider.Setting;
import lt.marius.converter.transactions.TransactionsGroup;
import lt.marius.converter.transactions.TransactionsGroupsController;
import lt.marius.converter.user.UsersController;
import lt.marius.converter.utils.DatabaseUtils;
import lt.marius.converter.utils.UIUtils;
import lt.marius.converter.views.CurrenciesDialog;
import lt.marius.converter.views.CurrenciesDialog.CurrenciesDialogListener;

public class ChartFragment extends Fragment implements SeriesFragment.SeriesFragmentCallback {

	static final int TYPE_LINE_CHART = 1;
	static final int TYPE_PIE_CHART = 2;
	
	static final int TYPE_EXPENSES = 1;
	static final int TYPE_INCOME = 2;
	
	private int showedType = TYPE_EXPENSES;
	
	private LayoutInflater mInflater; 
	private ProgressBar progress;
	private View mainLayout;
	private Chart mChart;
	private PieChart pieChart;
	
	private AsyncTask<Void, Integer, Float> showTask;
	
	private Button buttonLeft;
	private Button buttonRight;
	private TextView infoView;
	private CheckedTextView expensesInfoView, incomeInfoView;
	
	private int month = -1, year = -1;
	
	private Currency showedCurrency;
	
	private OnClickListener buttonClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (v == buttonLeft) {
				month--;
				if (month < Calendar.JANUARY) {
					year--;
					month = Calendar.DECEMBER;
				}
				refresh();
			} else if (v == buttonRight) {
				month++;
				if (month > Calendar.DECEMBER) {
					month = Calendar.JANUARY;
					year++;
				}
				refresh();
			}
		}
	};
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

//        expensesGroups.addAll(TransactionsGroupsController.getInstance().getExpensesGroups());
//        incomeGroups.addAll(TransactionsGroupsController.getInstance().getIncomeGroups());
	}
	
	private boolean refreshOnResume = false;
	@Override
	public void onResume() {
		super.onResume();
		if (refreshOnResume) {
			String prev = "";
			if (showedCurrency != null) {
				prev = showedCurrency.getCurrencyCodeShort();
			}
			Currency curr = UsersController.getInstance().getUserSelectedCurrency();
			if (curr == null || !prev.equals(curr.getShortCode())) {
				showedCurrency = curr;
				refresh();
			}
			refreshOnResume = false;
		}
	}
	
	@Override
	public void onPause() {
		refreshOnResume = true;
		super.onPause();
	}
	
	private MenuItem lineChartItem, pieChartItem;
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		lineChartItem = menu.findItem(R.id.menu_chart_line);
		pieChartItem = menu.findItem(R.id.menu_chart_pie);
		int type = SettingsProvider.getStored().getSetting(Setting.CHART_TYPE, Integer.class);
		if (type == TYPE_LINE_CHART) {
			lineChartItem.setVisible(false);
			pieChartItem.setVisible(true);
		} else {
			pieChartItem.setVisible(false);
			lineChartItem.setVisible(true);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_chart_line) {
				mChart = (Chart) mainLayout.findViewById(R.id.custom_chart);
				mChart.setVisibility(View.VISIBLE);
				if (pieChart != null) {
					pieChart.setVisibility(View.GONE);
					pieChart = null;
				}
				lineChartItem.setVisible(false);
				pieChartItem.setVisible(true);
				SettingsProvider.getStored().putSetting(Setting.CHART_TYPE, TYPE_LINE_CHART);
				refresh();
		} else if (item.getItemId() == R.id.menu_chart_pie){
				pieChart = (PieChart) mainLayout.findViewById(R.id.pie_chart);
				pieChart.setVisibility(View.VISIBLE);
				if (mChart != null) {
					mChart.setVisibility(View.GONE);
					mChart = null;
				}
				lineChartItem.setVisible(true);
				pieChartItem.setVisible(false);
				SettingsProvider.getStored().putSetting(Setting.CHART_TYPE, TYPE_PIE_CHART);
				refresh();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (month != -1)
			outState.putInt("chart_month", month);
		if (year != -1)
			outState.putInt("chart_year", year);
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_chart, container, false);
		mainLayout = v;
		int type = SettingsProvider.getStored().getSetting(Setting.CHART_TYPE, Integer.class);
		if (type == TYPE_LINE_CHART) {
			mChart = (Chart) v.findViewById(R.id.custom_chart);
			v.findViewById(R.id.pie_chart).setVisibility(View.GONE);
		} else {
			v.findViewById(R.id.custom_chart).setVisibility(View.GONE);
			pieChart = (PieChart)v.findViewById(R.id.pie_chart);
		}
		mInflater = inflater;
		v.findViewById(R.id.rl_buttons_bar).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
			}
		});
//		mainLayout.setOnClickListener(infoClick);
		buttonLeft = (Button)v.findViewById(R.id.button_month_left);
		buttonRight = (Button)v.findViewById(R.id.button_month_right);
        enableButtons();
		progress = (ProgressBar)v.findViewById(R.id.progress);
		infoView = (TextView)v.findViewById(R.id.text_info);
		expensesInfoView = (CheckedTextView)v.findViewById(R.id.text_info_expenses);
		incomeInfoView = (CheckedTextView)v.findViewById(R.id.text_info_income);
		

		
		if (savedInstanceState != null) {
			month = savedInstanceState.getInt("chart_month", -1);
			year = savedInstanceState.getInt("chart_year", -1);
		} else {
			month = year = -1;
		}
		if (month == -1) {
			month = Calendar.getInstance().get(Calendar.MONTH);
		}
		if (year == -1 || year < 1900) {	//sanity check. There is a bug lurking somewhere..
			year = Calendar.getInstance().get(Calendar.YEAR);
		}
		if (pieChart == null && mChart != null) {
			showTask = new ShowLineChartTask();
		} else {
			showTask = new ShowPieTask();
		}
		
		//get the selected currency
		showedCurrency = UsersController.getInstance().getUserSelectedCurrency();
		
		showTask.execute();
		
		
		return v;
	}

    private OnClickListener lineChartInfoViewClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            boolean expShowing = (showedType & TYPE_EXPENSES) == TYPE_EXPENSES;
            boolean incShowing = (showedType & TYPE_INCOME) == TYPE_INCOME;
            if (v == expensesInfoView) {
                expShowing = !expShowing;
            } else if (v == incomeInfoView) {
                incShowing = !incShowing;
            }
            setShowing(expShowing, incShowing);

            if (expShowing) {
                showedType |= TYPE_EXPENSES;
            } else {
                showedType &= ~TYPE_EXPENSES;
            }
            if (incShowing) {
                showedType |= TYPE_INCOME;
            } else {
                showedType &= ~TYPE_INCOME;
            }

            if (mChart != null) {
                mChart.clearSeries();
                ChartController controller = new ChartController(getActivity().getApplicationContext());
                float res = 0;
                if (showedType == (TYPE_EXPENSES | TYPE_INCOME)) {
                    mChart.addSeries(seriesExp);
                    mChart.addSeries(seriesInc);
                    res = Math.max(controller.getMaximumY(seriesExp), controller.getMaximumY(seriesInc));
                } else if (showedType == TYPE_EXPENSES) {
                    mChart.addSeries(seriesExp);
                    res = controller.getMaximumY(seriesExp);
                } else if (showedType == TYPE_INCOME) {
                    mChart.addSeries(seriesInc);
                    res = controller.getMaximumY(seriesInc);
                }
                Axis ya = new YAxis(0, Math.max(50, res / 5 * 5 + 5), 5, getActivity());
                mChart.setYAxis(ya);
                mChart.invalidate();
            } else {
                refresh();  //safety
            }
        }

    };

    private OnClickListener pieChartInfoViewClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v == expensesInfoView) {
                setShowing(true, false);
                if ((showedType & TYPE_EXPENSES) != TYPE_EXPENSES) {
                    showedType = TYPE_EXPENSES;
                    refresh();
                }
                showedType = TYPE_EXPENSES;
            } else if (v == incomeInfoView) {
                setShowing(false, true);
                if ((showedType & TYPE_INCOME) != TYPE_INCOME) {
                    showedType = TYPE_INCOME;
                    refresh();
                }
                showedType = TYPE_INCOME;
            }
        }

    };
	
	private void setShowing(boolean expenses, boolean income) {
		expensesInfoView.setChecked(expenses);
		expensesInfoView.setTextAppearance(getActivity(), 
				expenses ? R.style.ChartExpensesTextSelected : R.style.ChartExpensesText);
		incomeInfoView.setChecked(income);
		incomeInfoView.setTextAppearance(getActivity(), 
				income ? R.style.ChartIncomeTextSelected : R.style.ChartIncomeText);

	}
	
	private OnClickListener infoClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (showedCurrency == null) return;
			CurrenciesDialog dialog = new CurrenciesDialog();
			Bundle args = new Bundle();
			args.putString(CurrenciesDialog.TITLE, getString(R.string.choose_currency_for_chart));
			args.putString(CurrenciesDialog.DEFAULT_CURR_ID, showedCurrency.getCurrencyCodeShort());
			dialog.setArguments(args);
			dialog.setListener(new CurrenciesDialogListener() {
				
				@Override
				public void onCurrencySelected(Currency currency) {
					if (!currency.equals(showedCurrency)) {
						UsersController.getInstance().updateUserSelectedCurrency(currency);
						showedCurrency = UsersController.getInstance().getUserSelectedCurrency();
						UIUtils.runOnUiThread(new Runnable() {	//let the dialog close
							public void run() {
								refresh();
							}
						});
					}
				}
			});
			dialog.show(getFragmentManager(), "currencies_dialog");
		}
	};
	
	private String getSymbol() {
		if (showedCurrency == null) {	//safety
			return "";
		}
		String code = showedCurrency.getCurrencyCodeShort();
		
		try {
			IsoCode iso = IsoCode.valueOf(code);
			return iso.getSign();
		} catch (IllegalArgumentException e) {
			//ignore
		}
		return code;
	}
	
	private void updateInfoTexts(double income, double expenses) {
		infoView.setText(MONTH_NAMES[month] + '\n' + year);
		expensesInfoView.setText( 
				String.format("%s %.2f%s", getString(R.string.expenses_short), expenses, getSymbol()));
		incomeInfoView.setText(
				String.format("%s %.2f%s", getString(R.string.income_short), income, getSymbol()));
	}
	
	private void disableButtons() {
        buttonLeft.setOnClickListener(null);

        buttonRight.setOnClickListener(null);
    }

    private void enableButtons() {
        buttonLeft.setOnClickListener(buttonClick);
        buttonRight.setOnClickListener(buttonClick);
        Calendar cal = Calendar.getInstance();
        if (year >= cal.get(Calendar.YEAR) && month == cal.get(Calendar.MONTH) ) {
            buttonRight.setEnabled(false);

        } else {
            buttonRight.setEnabled(true);
        }
    }
	
	private final String[] MONTH_NAMES = UIUtils.getLocalizedMonths(Locale.getDefault(), false);
    private CombinedSeries2D seriesExp, seriesInc;

	class ShowLineChartTask extends AsyncTask<Void, Integer, Float> {

		private ChartController controller;

    	
    	public ShowLineChartTask() {
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		progress.setVisibility(View.VISIBLE);
    		refreshing = true;
    		disableButtons();
    		controller = new ChartController(getActivity().getApplicationContext());
    	}

    	
		@Override
		protected Float doInBackground(Void... params) {
			
			try {
                seriesExp = seriesInc = null;

                seriesExp = new CombinedSeries2D(
                        controller.getDailyExpenses(month, year, showedCurrency, null));
                seriesInc = new CombinedSeries2D(
                        controller.getDailyIncome(month, year, showedCurrency, null));
			} catch (IllegalStateException ex) {
				DatabaseUtils.closeDatabase();
				DatabaseUtils.initDatabse(getActivity().getApplicationContext());
				return null;
			}

			return Math.max(seriesExp == null ? 0 : controller.getMaximumY(seriesExp),
                    seriesInc == null ? 0 : controller.getMaximumY(seriesInc));
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(Float result) {
			progress.setVisibility(View.GONE);
			if (!isAdded() || result == null) return;
			
			if (mChart != null) {
                expensesInfoView.setOnClickListener(lineChartInfoViewClickListener);
                incomeInfoView.setOnClickListener(lineChartInfoViewClickListener);
				updateInfoTexts(controller.getIncomeSum(), controller.getExpensesSum());
				Axis ya = new YAxis(0, Math.max(50, result / 5 * 5 + 5), 5, getActivity());
	//          ya.setVisible(false);
				Calendar cal = Calendar.getInstance();
				cal.set(year, month, 1);
				int end = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	    		Axis xa = new XAxis(1, end, 1, getActivity());
	    		mChart.clearSeries();
				mChart.setYAxis(ya);
	    		mChart.setXAxis(xa);

				if (seriesExp != null && (showedType & TYPE_EXPENSES) == TYPE_EXPENSES) {
                    mChart.addSeries(seriesExp);
				}
                if (seriesInc != null && (showedType & TYPE_INCOME) == TYPE_INCOME) {
                    mChart.addSeries(seriesInc);
                }
//				mChart.showLegend(false);
                mChart.setOnClickListener(chartClickListener);
				mChart.invalidate();
			}
			enableButtons();
			refreshing = false;
		}
    	
    }

    private OnClickListener chartClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            int[] ids = {};
            boolean[] checks = {};
            if (seriesInc != null && seriesExp != null) {
                int[] incIds = seriesInc.getGroupsIds();
                int[] expIds = seriesExp.getGroupsIds();
                ids = new int[incIds.length + expIds.length];
                for (int i = 0; i < expIds.length; i++) {
                    ids[i] = expIds[i];
                }
                for (int i = 0; i < incIds.length; i++) {
                    ids[i + expIds.length] = incIds[i];
                }
                boolean[] incChecks = seriesInc.getGroupsVisibility();
                boolean[] expChecks = seriesExp.getGroupsVisibility();
                checks = new boolean[incChecks.length + expChecks.length];
                for (int i = 0; i < expChecks.length; i++) {
                    checks[i] = expChecks[i];
                }
                for (int i = 0; i < incChecks.length; i++) {
                    checks[i + expChecks.length] = incChecks[i];
                }

            } else if (seriesExp != null) {
                ids = seriesExp.getGroupsIds();
                checks = seriesExp.getGroupsVisibility();
            } else if (seriesInc != null) {
                ids = seriesInc.getGroupsIds();
                checks = seriesInc.getGroupsVisibility();
            }
            if (ids.length > 0 && checks.length > 0) {
                showSeriesDialogFragment(ids, checks);
            }
        }
    };

    private List<SeriesPie> sp;
	class ShowPieTask extends AsyncTask<Void, Integer, Float> {

		private ChartController controller;

    	@Override
    	protected void onPreExecute() {
            if (showedType != TYPE_EXPENSES && showedType != TYPE_INCOME) {
                showedType = TYPE_EXPENSES;
                setShowing(true, false);
            }
    		progress.setVisibility(View.VISIBLE);
    		refreshing = true;
    		disableButtons();
    		controller = new ChartController(getActivity().getApplicationContext());
    	}

    	
		@Override
		protected Float doInBackground(Void... params) {
			try {
				if ((showedType & TYPE_EXPENSES) == TYPE_EXPENSES) {
					sp = controller.getMonthlyExpenses(month, year, showedCurrency);
				} else {
					sp = controller.getMonthlyIncome(month, year, showedCurrency);
				}
			} catch (IllegalStateException ex) {
				DatabaseUtils.closeDatabase();
				DatabaseUtils.initDatabse(getActivity().getApplicationContext());
				return null;
			}
			return Float.valueOf(0);
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(Float result) {
			progress.setVisibility(View.GONE);
			if (!isAdded() || result == null) return;
			
			if (pieChart != null) {
                expensesInfoView.setOnClickListener(pieChartInfoViewClickListener);
                incomeInfoView.setOnClickListener(pieChartInfoViewClickListener);
				updateInfoTexts(controller.getIncomeSum(), controller.getExpensesSum());
				pieChart.setSeries(sp);
//				pieChart.showLegend(UIUtils.getCurrRotation(getActivity()).equals(Rotation.PORTRAIT));

                pieChart.setOnClickListener(pieClickListener);
				pieChart.invalidate();
			}
			enableButtons();
			refreshing = false;
		}
    	
    }

    private OnClickListener pieClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (sp == null) return;

            int[] ids = new int[sp.size()];
            boolean[] checks = new boolean[sp.size()];
            int i = 0;
            for (SeriesPie s : sp) {
                checks[i] = s.isVisible();
                ids[i++] = s.getGroupId();
            }
            showSeriesDialogFragment(ids, checks);
        }
    };

    private void showSeriesDialogFragment(int[] ids, boolean[] checks) {
        SeriesFragment frag = new SeriesFragment();
        Bundle args = new Bundle();
        args.putIntArray(SeriesFragment.GROUP_PARENT_IDS, ids);
        args.putBooleanArray(SeriesFragment.INITIAL_CHECHKED_STATE, checks);
        frag.setArguments(args);
        frag.show(getChildFragmentManager(), getTag() + "#dialog");
    }

    public void onItemChecked(TransactionsGroup group, boolean checked) {
        if (mChart != null) {
            if (seriesExp != null) {
                seriesExp.setVisibility(group.getId(), checked);
            }
            if (seriesInc != null) {
                seriesInc.setVisibility(group.getId(), checked);
            }
//            final TransactionsGroup expGroup
//                    = TransactionsGroupsController.getInstance().getDefaultExpensesGroup();
//            if (group.getParent().equals(expGroup)) {
//                if (checked) {
//                    expensesGroups.add(group);
//                } else {
//                    expensesGroups.remove(group);
//                }
//            } else {
//                if (checked) {
//                    incomeGroups.add(group);
//                } else {
//                    incomeGroups.remove(group);
//                }
//            }
            mChart.invalidate();
        } else if (pieChart != null){
            for (SeriesPie series : sp) {
                if (series.getGroupId() == group.getId()) {
                    pieChart.toggleSeriesVisibility(series, checked);
                }
            }
        }
    }

	private boolean refreshing = false;
	
	public void refresh() {
		if (!refreshing) {
			if (mChart != null) {
				showTask = new ShowLineChartTask();
				showTask.execute();
			} else if (pieChart != null) {
				showTask = new ShowPieTask();
				showTask.execute();
			}
		}
	}


}
