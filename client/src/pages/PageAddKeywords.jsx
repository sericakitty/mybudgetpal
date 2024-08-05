import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../services';
import PropTypes from 'prop-types';
import { useNotification } from '../components/TheNotificationContext';

const PageAddKeywords = ({ type }) => {
  const navigate = useNavigate();
  const [category, setCategory] = useState('');
  const [keywords, setKeywords] = useState([]);
  const { showNotification } = useNotification();

  const handleKeywordChange = (e) => {
    setKeywords(e.target.value.split(',').map((keyword) => keyword.trim()));
  };

  const handleSave = async () => {
    try {
      const requestBody = {
        category: type === 'INCLUDED' ? category : 'Excluded',
        keywords,
        type,
      };

      await axiosInstance.post('/user/keywords/add', requestBody);
      showNotification('Keywords added successfully!', 'success');
      navigate('/keywords');
    } catch (error) {
      showNotification('Failed to add keyword', 'error');
    }
  };

  const handleCancel = () => {
    navigate('/keywords');
  };

  return (
    <div className="container mt-5">
      <h2>Add {type === 'INCLUDED' ? 'Included' : 'Excluded'} Keywords</h2>
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

PageAddKeywords.propTypes = {
  type: PropTypes.oneOf(['INCLUDED', 'EXCLUDED']).isRequired,
};

export default PageAddKeywords;
