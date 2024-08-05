import { useState, useEffect } from 'react';
import axiosInstance from '../services';
import { makeStyles } from '@material-ui/core/styles';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Button } from '@material-ui/core';

const useStyles = makeStyles({
  container: {
    marginTop: '20px',
  },
  table: {
    minWidth: 650,
  },
  title: {
    marginBottom: '20px',
  },
});

const EntryList = () => {
  const classes = useStyles();
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchEntries = async () => {
      try {
        const response = await axiosInstance.get('/api/entries');
        setEntries(response.data);
      } catch (error) {
        console.error('Error fetching entries:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchEntries();
  }, []);

  const handleDelete = async (id) => {
    if (window.confirm('Do you want to delete this entry?')) {
      try {
        await axiosInstance.get(`/api/entries/delete/${id}`);
        setEntries(entries.filter(entry => entry.id !== id));
      } catch (error) {
        console.error('Error deleting entry:', error);
      }
    }
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <div className="container">
      <h2 className={classes.title}>Entries</h2>
      <TableContainer component={Paper} className={classes.container}>
        <Table className={classes.table} aria-label="entry table">
          <TableHead>
            <TableRow>
              <TableCell>Date</TableCell>
              <TableCell align="right">Amount €</TableCell>
              <TableCell>Title</TableCell>
              <TableCell>Bank</TableCell>
              <TableCell>Reference Id</TableCell>
              <TableCell align="right"></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {entries.map((entry) => (
              <TableRow key={entry.id}>
                <TableCell component="th" scope="row">
                  {entry.date}
                </TableCell>
                <TableCell align="right">{entry.amount}</TableCell>
                <TableCell>{entry.title}</TableCell>
                <TableCell>{entry.bankName}</TableCell>
                <TableCell>{entry.referenceId}</TableCell>
                <TableCell align="right">
                  <Button
                    variant="contained"
                    color="secondary"
                    onClick={() => handleDelete(entry.id)}
                  >
                    Delete
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </div>
  );
};

export default EntryList;
