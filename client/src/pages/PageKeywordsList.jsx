import { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import axiosInstance from '../services';
import { useNotification } from '../components/TheNotificationContext';

const Keywords = () => {

  const [includedKeywords, setIncludedKeywords] = useState([]);
  const [excludedKeywords, setExcludedKeywords] = useState([]);
  const [loading, setLoading] = useState(true);
  const { showNotification } = useNotification();

  const fetchData = async () => {
    try {
      const response = await axiosInstance.get('/user/keywords');
      setIncludedKeywords(response.data.includedKeywords);
      setExcludedKeywords(response.data.excludedKeywords);
      setLoading(false);
    } catch (error) {
      showNotification('Failed to fetch category keywords', 'error');
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const deleteKeyword = async (id) => {
    try {
      await axiosInstance.get(`/user/keywords/delete/${id}`);
      showNotification('Keyword deleted successfully!', 'success');
      fetchData();
    } catch (error) {
      console.error('Error:', error);
      showNotification('Failed to delete keyword', 'error');
    }
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <div className="container">
      <div className="d-flex justify-content-between">
        <h2>
          Category Keywords Include
          <Link to="/keywords/new" className="btn btn-primary">Add new</Link>
        </h2>
      </div>

      {includedKeywords.length > 0 && (
        <table className="table">
          <thead>
            <tr>
              <th>Category</th>
              <th>Keywords</th>
              <th></th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {includedKeywords.map(categoryKeyword => (
              <tr key={categoryKeyword.id}>
                <td>{categoryKeyword.category}</td>
                <td>
                  {categoryKeyword.keywords ? categoryKeyword.keywords.join(', ') : 'No keywords'}
                </td>
                <td>
                  <Link to={`/keywords/edit/${categoryKeyword.id}`} className="btn btn-primary">Edit</Link>
                </td>
                <td>
                  <button className="btn btn-danger" onClick={(e) => {
                    if (window.confirm('Do you want to delete this category')) {
                      e.preventDefault();
                      deleteKeyword(categoryKeyword.id);
                    }
                  }}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <div>
        <div className="d-flex justify-content-between">
          <h2>
            Excluded Keywords
            <Link to="/keywords/new/excluded" className="btn btn-primary">Add new</Link>
          </h2>
        </div>

        {excludedKeywords.length > 0 && (
          <table className="table">
            <thead>
              <tr>
                <th>Keywords</th>
                <th></th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {excludedKeywords.map(k => (
                <tr key={k.id}>
                  <td>{k.keywords.join(', ')}</td>
                  <td>
                    <Link to={`/keywords/edit/excluded/${k.id}`} className="btn btn-primary">Edit</Link>
                  </td>
                  <td>
                    <button className="btn btn-danger" onClick={(e) => {
                      if (window.confirm('Do you want to delete this keyword')) {
                        e.preventDefault();
                        deleteKeyword(k.id);
                      }
                    }}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

Keywords.propTypes = {
  categoryKeywordsIncluded: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.number.isRequired,
    category: PropTypes.string.isRequired,
    keywords: PropTypes.arrayOf(PropTypes.string)
  })),
  keywordsExcluded: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.number.isRequired,
    keyword: PropTypes.string.isRequired
  })),
  user: PropTypes.shape({
    username: PropTypes.string,
  }),
};

export default Keywords;
