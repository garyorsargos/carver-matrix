import React, { useState, useEffect } from 'react';
import { Box, TextField, Select, MenuItem, FormControl, InputLabel } from '@mui/material';
import axios from 'axios';
import MiniMatrixCard from './miniMatrixCard';

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

    console.log('Matrix:', matrix.name);
    console.log('Current user email:', userEmail);
    console.log('Hosts:', matrix.hosts);
    console.log('Is host:', isHost);
    console.log('Is participant:', isParticipant);
    console.log('Is both:', isBoth);
    console.log('Role filter:', roleFilter);

    const matchesRole = 
      (roleFilter === 'host' && isHost) ||
      (roleFilter === 'participant' && isParticipant) ||
      (roleFilter === 'both' && isBoth);

    console.log('Matches role:', matchesRole);
    console.log('Matches search:', matchesSearch);
    console.log('Final result:', matchesSearch && matchesRole);

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
        gap: 1,
        p: 1,
        backgroundColor: '#ffffff',
        borderRadius: 1,
        overflowX: 'hidden',
        boxSizing: 'border-box',
      }}
    >
      <TextField
        size="small"
        placeholder="Search matrices..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        sx={{ 
          mb: 1,
          '& .MuiInputBase-input': {
            color: 'black',
          },
          '& .MuiOutlinedInput-notchedOutline': {
            borderColor: 'rgba(0, 0, 0, 0.23)',
          },
          '&:hover .MuiOutlinedInput-notchedOutline': {
            borderColor: 'rgba(0, 0, 0, 0.87)',
          },
        }}
      />

      <FormControl size="small" sx={{ mb: 1 }}>
        <InputLabel sx={{ color: 'black' }}>Role Filter</InputLabel>
        <Select
          value={roleFilter}
          label="Role Filter"
          onChange={(e) => setRoleFilter(e.target.value)}
          sx={{
            '& .MuiOutlinedInput-notchedOutline': {
              borderColor: 'rgba(0, 0, 0, 0.23)',
            },
            '&:hover .MuiOutlinedInput-notchedOutline': {
              borderColor: 'rgba(0, 0, 0, 0.87)',
            },
            '& .MuiSelect-select': {
              color: 'black',
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
          gap: 1,
          boxSizing: 'border-box',
        }}
      >
        {filteredMatrices.map((matrix) => (
          <MiniMatrixCard
            key={matrix.matrixId}
            title={matrix.name}
            description={matrix.description}
            onSelectMatrix={() => handleMatrixSelect(matrix.matrixId)}
            titleColor="black"
          />
        ))}
      </Box>
    </Box>
  );
};

export default MatrixExplorer;

