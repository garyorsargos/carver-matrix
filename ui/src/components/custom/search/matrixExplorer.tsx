import React, { useState, useEffect } from 'react';
import { 
  Box, 
  TextField, 
  FormControl, 
  Typography, 
  Radio, 
  RadioGroup, 
  FormControlLabel,
  Collapse,
  IconButton,
  FormGroup,
  Checkbox,
  Paper
} from '@mui/material';
import axios from 'axios';
import MiniMatrixCard from './miniMatrixCard';
import SearchIcon from '@mui/icons-material/Search';
import FilterListIcon from '@mui/icons-material/FilterList';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';

interface CarverMatrix {
  matrixId: number;
  name: string;
  description: string;
  hosts: string[];
  participants: string[];
  roleBased: boolean;
}

const MatrixExplorer: React.FC = () => {
  const [matrices, setMatrices] = useState<CarverMatrix[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [roleBasedFilter, setRoleBasedFilter] = useState<'all' | 'enabled' | 'disabled'>('all');
  const [userEmail, setUserEmail] = useState<string | null>(null);
  const [filtersOpen, setFiltersOpen] = useState(false);
  const [roleFilters, setRoleFilters] = useState({
    host: false,
    participant: false,
    both: false,
  });

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

  const handleRoleFilterChange = (role: keyof typeof roleFilters) => {
    setRoleFilters(prev => ({
      ...prev,
      [role]: !prev[role]
    }));
  };

  const filteredMatrices = matrices.filter((matrix) => {
    // Text search filter
    const term = searchTerm.toLowerCase();
    const matchesSearch = matrix.name.toLowerCase().includes(term) ||
      matrix.description.toLowerCase().includes(term);

    // Role-based matrix filter
    if (roleBasedFilter !== 'all') {
      if (roleBasedFilter === 'enabled' && !matrix.roleBased) {
        return false;
      }
      if (roleBasedFilter === 'disabled' && matrix.roleBased) {
        return false;
      }
    }

    // If no role filters are selected, only apply text search and role-based filter
    if (!roleFilters.host && !roleFilters.participant && !roleFilters.both) {
      return matchesSearch;
    }

    // If role filters are selected but matrix is not role-based, hide it
    if (!matrix.roleBased) {
      return false;
    }

    // Role-based filtering
    if (!userEmail) {
      console.log('No user email available');
      return false;
    }

    const isHost = matrix.hosts?.includes(userEmail) || false;
    const isParticipant = matrix.participants?.includes(userEmail) || false;
    const isBoth = isHost && isParticipant;

    // Modified role filtering to prevent duplicates
    let matchesRole = false;
    if (roleFilters.both) {
      matchesRole = isBoth;
    } else {
      if (roleFilters.host) {
        matchesRole = isHost;
      }
      if (roleFilters.participant) {
        matchesRole = matchesRole || isParticipant;
      }
      if (!roleFilters.host && !roleFilters.participant) {
        matchesRole = true;
      }
    }

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

      <Box>
        <Box 
          sx={{ 
            display: 'flex', 
            alignItems: 'center', 
            justifyContent: 'space-between',
            cursor: 'pointer',
            p: 1,
            borderRadius: 1,
            '&:hover': {
              backgroundColor: 'rgba(255, 255, 255, 0.05)',
            },
          }}
          onClick={() => setFiltersOpen(!filtersOpen)}
        >
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <FilterListIcon sx={{ color: 'rgba(255, 255, 255, 0.7)', mr: 1 }} />
            <Typography
              variant="subtitle2"
              sx={{
                color: 'rgba(255, 255, 255, 0.7)',
                fontWeight: "bold",
                textTransform: "uppercase",
                letterSpacing: "1px",
              }}
            >
              Filters
            </Typography>
          </Box>
          <IconButton size="small" sx={{ color: 'rgba(255, 255, 255, 0.7)' }}>
            {filtersOpen ? <ExpandLessIcon /> : <ExpandMoreIcon />}
          </IconButton>
        </Box>

        <Collapse in={filtersOpen}>
          <Paper
            sx={{
              p: 2,
              mt: 1,
              backgroundColor: 'rgba(255, 255, 255, 0.05)',
              backdropFilter: 'blur(10px)',
              border: '1px solid rgba(255, 255, 255, 0.1)',
            }}
          >
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
              <Box>
                <Typography
                  variant="subtitle2"
                  sx={{
                    color: 'rgba(255, 255, 255, 0.7)',
                    fontWeight: "bold",
                    textTransform: "uppercase",
                    letterSpacing: "1px",
                    mb: 2,
                  }}
                >
                  Filter by Role
                </Typography>
                <FormControl component="fieldset" sx={{ width: '100%' }}>
                  <FormGroup>
                    <FormControlLabel
                      control={
                        <Checkbox
                          checked={roleFilters.host}
                          onChange={() => handleRoleFilterChange('host')}
                          sx={{
                            color: 'rgba(255, 255, 255, 0.7)',
                            '&.Mui-checked': {
                              color: '#014093',
                            },
                          }}
                        />
                      }
                      label={
                        <Typography sx={{ color: '#ffffff' }}>Host</Typography>
                      }
                    />
                    <FormControlLabel
                      control={
                        <Checkbox
                          checked={roleFilters.participant}
                          onChange={() => handleRoleFilterChange('participant')}
                          sx={{
                            color: 'rgba(255, 255, 255, 0.7)',
                            '&.Mui-checked': {
                              color: '#014093',
                            },
                          }}
                        />
                      }
                      label={
                        <Typography sx={{ color: '#ffffff' }}>Participant</Typography>
                      }
                    />
                    <FormControlLabel
                      control={
                        <Checkbox
                          checked={roleFilters.both}
                          onChange={() => handleRoleFilterChange('both')}
                          sx={{
                            color: 'rgba(255, 255, 255, 0.7)',
                            '&.Mui-checked': {
                              color: '#014093',
                            },
                          }}
                        />
                      }
                      label={
                        <Typography sx={{ color: '#ffffff' }}>Host & Participant</Typography>
                      }
                    />
                  </FormGroup>
                </FormControl>
              </Box>

              <Box>
                <Typography
                  variant="subtitle2"
                  sx={{
                    color: 'rgba(255, 255, 255, 0.7)',
                    fontWeight: "bold",
                    textTransform: "uppercase",
                    letterSpacing: "1px",
                    mb: 2,
                  }}
                >
                  Role-Based Matrix Filter
                </Typography>
                <FormControl component="fieldset" sx={{ width: '100%' }}>
                  <RadioGroup
                    value={roleBasedFilter}
                    onChange={(e) => setRoleBasedFilter(e.target.value as 'all' | 'enabled' | 'disabled')}
                    sx={{
                      '& .MuiRadio-root': {
                        color: 'rgba(255, 255, 255, 0.7)',
                        '&.Mui-checked': {
                          color: '#014093',
                        },
                      },
                    }}
                  >
                    <FormControlLabel
                      value="all"
                      control={<Radio />}
                      label={
                        <Typography sx={{ color: '#ffffff' }}>All Matrices</Typography>
                      }
                    />
                    <FormControlLabel
                      value="enabled"
                      control={<Radio />}
                      label={
                        <Typography sx={{ color: '#ffffff' }}>Role-Based Only</Typography>
                      }
                    />
                    <FormControlLabel
                      value="disabled"
                      control={<Radio />}
                      label={
                        <Typography sx={{ color: '#ffffff' }}>Non-Role-Based Only</Typography>
                      }
                    />
                  </RadioGroup>
                </FormControl>
              </Box>
            </Box>
          </Paper>
        </Collapse>
      </Box>

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

