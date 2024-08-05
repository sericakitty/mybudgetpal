import { useEffect, useState } from 'react';
import axiosInstance from '../services';
import { BarChart, Bar, XAxis, YAxis, Tooltip, LabelList } from 'recharts';
import { makeStyles } from '@material-ui/core/styles';
import { Paper, Select, MenuItem, FormControl, InputLabel } from '@material-ui/core';
import _ from 'lodash';

const useStyles = makeStyles({
  container: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    padding: '20px',
  },
  chartContainer: {
    position: 'relative',
    width: '100%',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
  },
  header: {
    marginRight: 'auto',
    marginLeft: 20,
  },
  chart: {
    marginRight: 30,
    marginLeft: 20,
  },
  formControl: {
    margin: 10,
    minWidth: 120,
  },
});

const StatisticList = () => {
  const classes = useStyles();
  const [expenses, setExpenses] = useState([]);
  const [balance, setBalance] = useState(0);
  const [monthlyExpenses, setMonthlyExpenses] = useState({});
  const [selectedMonth, setSelectedMonth] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchExpenses = async () => {
      try {
        const response = await axiosInstance.get('/api/statistics');
        const { categoryExpenses, balance, monthlyExpenses } = response.data;
        const groupedData = Object.entries(categoryExpenses).map(([category, amount]) => ({
          category,
          amount,
        }));
        setExpenses(groupedData);
        setBalance(balance);
        setMonthlyExpenses(monthlyExpenses);

        // Set the default selected month to the current month
        const currentMonth = new Date().toLocaleString('default', { month: 'long' }).toUpperCase();
        if (monthlyExpenses[currentMonth]) {
          setSelectedMonth(currentMonth);
          const sortedExpenses = sortCategories(monthlyExpenses[currentMonth]);
          setExpenses(sortedExpenses);
        } else if (Object.keys(monthlyExpenses).length > 0) {
          const firstMonth = Object.keys(monthlyExpenses)[0];
          setSelectedMonth(firstMonth);
          const sortedExpenses = sortCategories(monthlyExpenses[firstMonth]);
          setExpenses(sortedExpenses);
        }
      } catch (error) {
        console.error('Error fetching statistics:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchExpenses();
  }, []);

  const handleMonthChange = (event) => {
    const month = event.target.value;
    setSelectedMonth(month);
    const sortedExpenses = sortCategories(monthlyExpenses[month]);
    setExpenses(sortedExpenses);
  };

  const sortCategories = (data) => {
    const sortedData = _.sortBy(Object.entries(data).map(([category, amount]) => ({
      category,
      amount,
    })), [(entry) => entry.category === 'Other Income' ? 2 : entry.category === 'Other Expenses' ? 1 : 0]);
    return sortedData;
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <Paper className={classes.container}>
      <h3 className={classes.header}>Statistics</h3>
      <p className={classes.header}>Balance: {balance} €</p>
      <FormControl className={classes.formControl}>
        <InputLabel id="month-select-label">Month</InputLabel>
        <Select
          labelId="month-select-label"
          id="month-select"
          value={selectedMonth}
          onChange={handleMonthChange}
        >
          {Object.keys(monthlyExpenses).map((month) => (
            <MenuItem key={month} value={month}>
              {month}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      <div className={classes.chartContainer}>
        <BarChart
          width={window.innerWidth - 50}
          height={window.innerHeight - 500}
          data={expenses}
          className={classes.chart}
          margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
        >
          <XAxis dataKey="category" />
          <YAxis
            label={{ value: 'Amount (€)', angle: -90, position: 'insideLeft', style: { textAnchor: 'middle', fill: 'black' } }}
          />
          <Tooltip />
          <Bar dataKey="amount" fill="#8884d8">
            <LabelList dataKey="amount" position="top" style={{ fill: 'black', fontSize: 12 }} />
          </Bar>
        </BarChart>
      </div>
    </Paper>
  );
};

export default StatisticList;
