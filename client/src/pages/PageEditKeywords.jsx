import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosInstance from '../services';
import PropTypes from 'prop-types';
import { useNotification } from '../components/TheNotificationContext';

const PageEditKeywords = ({ type }) => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [category, setCategory] = useState('');
  const [keywords, setKeywords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { showNotification } = useNotification();

  useEffect(() => {
    const fetchKeywordDetails = async () => {
      try {
        const response = await axiosInstance.get(`/user/keywords/edit/${id}`);
        const data = response.data;
        if (type === 'INCLUDED') {
          setCategory(data.category || '');
        }
        setKeywords(data.keywords || []);
        setLoading(false);
      } catch (error) {
        setError('Failed to fetch keyword details');
        setLoading(false);
      }
    };

    fetchKeywordDetails();
  }, [id, type]);

  const handleKeywordChange = (e) => {
    setKeywords(e.target.value.split(',').map((keyword) => keyword.trim()));
  };

  const handleSave = async () => {
    try {
      await axiosInstance.post(`/user/keywords/update/${id}`, {
        category: type === 'INCLUDED' ? category : 'Excluded',
        keywords,
        type,
      });
      showNotification('Keywords updated successfully!', 'success');
      navigate('/keywords');
    } catch (error) {
      showNotification('Failed to save keyword', 'error');
    }
  };

  const handleCancel = () => {
    navigate('/keywords');
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>{error}</div>;
  }

  return (
    <div className="container mt-5">
      <h2>Edit {type === 'INCLUDED' ? 'Included' : 'Excluded'} Keywords</h2>
      <div className="form-group">
        {type === 'INCLUDED' && (
          <>
            <label htmlFor="category">Category</label>
            <input
              type="text"
              id="category"
              className="form-control"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
            />
          </>
        )}
        <label htmlFor="keywords">Keywords (comma-separated)</label>
        <input
          type="text"
          id="keywords"
          className="form-control"
          value={keywords.join(', ')}
          onChange={handleKeywordChange}
        />
      </div>
      <button className="btn btn-primary" onClick={handleSave}>Save</button>
      <button className="btn btn-secondary ml-2" onClick={handleCancel}>Cancel</button>
    </div>
  );
};

PageEditKeywords.propTypes = {
  type: PropTypes.oneOf(['INCLUDED', 'EXCLUDED']).isRequired,
};

export default PageEditKeywords;
