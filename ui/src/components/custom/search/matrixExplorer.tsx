import React, { useState, useEffect } from 'react';
import { Box, TextField, Select, MenuItem, FormControl, InputLabel, Typography } from '@mui/material';
import axios from 'axios';
import MiniMatrixCard from './miniMatrixCard';
import SearchIcon from '@mui/icons-material/Search';
import FilterListIcon from '@mui/icons-material/FilterList';

interface CarverMatrix {
  matrixId: number;
  name: string;
  description: string;
  hosts: string[];
  participants: string[];
}

const MatrixExplorer: React.FC = () => {
  const [matrices, setMatrices] = useState<CarverMatrix[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [roleFilter, setRoleFilter] = useState<string>('all');
  const [userEmail, setUserEmail] = useState<string | null>(null);

  useEffect(() => {
    const fetchUserEmail = async () => {
      try {
        const response = await axios.get('/api/user2/whoami-upsert', { withCredentials: true });
        if (response.data.includes('{')) {
          const userData = JSON.parse(response.data.split('}{')[0] + '}');
          setUserEmail(userData.email);
        }
      } catch (error) {
        console.error('Error fetching user email:', error);
      }
    };

    const fetchMatrices = async () => {
      try {
        setIsLoading(true);
        const response = await axios.get('/api/carvermatrices/search', { withCredentials: true });
        let matrixData;
        if (response.data.includes('[')) {
          const parts = response.data.split(']{');
          matrixData = JSON.parse(parts[0] + ']');
          setMatrices(matrixData);
        }
      } catch (error) {
        console.error('Error fetching matrices:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchUserEmail();
    fetchMatrices();
  }, []);

  const filteredMatrices = matrices.filter((matrix) => {
    // Text search filter
    const term = searchTerm.toLowerCase();
    const matchesSearch = matrix.name.toLowerCase().includes(term) ||
      matrix.description.toLowerCase().includes(term);

    // If no role filter is selected, only apply text search
    if (roleFilter === 'all') {
      return matchesSearch;
    }

    // Role-based filtering
    if (!userEmail) {
      console.log('No user email available');
      return false;
    }

    const isHost = matrix.hosts?.includes(userEmail) || false;
    const isParticipant = matrix.participants?.includes(userEmail) || false;
    const isBoth = isHost && isParticipant;

    const matchesRole = 
      (roleFilter === 'host' && isHost) ||
      (roleFilter === 'participant' && isParticipant) ||
      (roleFilter === 'both' && isBoth);

    return matchesSearch && matchesRole;
  });

  const handleMatrixSelect = (matrixId: number) => {
    window.location.href = `/EditMatrix?matrixId=${matrixId}`;
  };

  if (isLoading) {
    return null;
  }

  return (
    <Box
      sx={{
        width: '100%',
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        gap: 2,
        p: 1.5,
        boxSizing: 'border-box',
      }}
    >
      <Typography
        variant="h6"
        sx={{
          color: "#ffffff",
          fontWeight: "bold",
          textTransform: "uppercase",
          letterSpacing: "1px",
          fontFamily: "'Roboto Condensed', sans-serif",
        }}
      >
        Matrix Explorer
      </Typography>

      <TextField
        fullWidth
        size="small"
        placeholder="Search matrices..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        InputProps={{
          startAdornment: (
            <SearchIcon sx={{ color: 'rgba(255, 255, 255, 0.7)', mr: 1 }} />
          ),
        }}
        sx={{ 
          '& .MuiOutlinedInput-root': {
            color: '#ffffff',
            '& fieldset': {
              borderColor: 'rgba(255, 255, 255, 0.23)',
            },
            '&:hover fieldset': {
              borderColor: '#014093',
            },
            '&.Mui-focused fieldset': {
              borderColor: '#014093',
            },
          },
          '& .MuiInputBase-input::placeholder': {
            color: 'rgba(255, 255, 255, 0.5)',
          },
        }}
      />

      <FormControl fullWidth size="small">
        <InputLabel sx={{ color: 'rgba(255, 255, 255, 0.7)' }}>Role Filter</InputLabel>
        <Select
          value={roleFilter}
          label="Role Filter"
          onChange={(e) => setRoleFilter(e.target.value)}
          startAdornment={<FilterListIcon sx={{ color: 'rgba(255, 255, 255, 0.7)', mr: 1 }} />}
          sx={{
            color: '#ffffff',
            '& .MuiOutlinedInput-notchedOutline': {
              borderColor: 'rgba(255, 255, 255, 0.23)',
            },
            '&:hover .MuiOutlinedInput-notchedOutline': {
              borderColor: '#014093',
            },
            '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
              borderColor: '#014093',
            },
            '& .MuiSelect-icon': {
              color: 'rgba(255, 255, 255, 0.7)',
            },
          }}
        >
          <MenuItem value="all">All Roles</MenuItem>
          <MenuItem value="host">Host</MenuItem>
          <MenuItem value="participant">Participant</MenuItem>
          <MenuItem value="both">Both</MenuItem>
        </Select>
      </FormControl>

      <Box
        sx={{
          flex: 1,
          overflowY: 'auto',
          overflowX: 'hidden',
          display: 'flex',
          flexDirection: 'column',
          gap: 1.5,
          boxSizing: 'border-box',
          pr: 0.5,
        }}
      >
        {filteredMatrices.map((matrix) => (
          <MiniMatrixCard
            key={matrix.matrixId}
            title={matrix.name}
            description={matrix.description}
            onSelectMatrix={() => handleMatrixSelect(matrix.matrixId)}
            titleColor="#ffffff"
          />
        ))}
      </Box>
    </Box>
  );
};

export default MatrixExplorer;

