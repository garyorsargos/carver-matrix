import React, { useEffect, useState } from "react";
import {
  Box,
  Typography,
  TextField,
  FormControl,
  FormGroup,
  FormControlLabel,
  Checkbox,
  Radio,
  RadioGroup,
  Button,
  InputAdornment,
  Paper,
  IconButton,
  Tooltip,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { ROUTES } from "../helpers/helpers";
import SearchIcon from '@mui/icons-material/Search';
import AddIcon from '@mui/icons-material/Add';
import FilterListIcon from '@mui/icons-material/FilterList';
import OpenInNewIcon from '@mui/icons-material/OpenInNew';
import DeleteIcon from '@mui/icons-material/Delete';
import { ExportPdfButton } from '../components/custom/pdfExport/ExportPdfButton';

interface CarverMatrix {
  matrixId: number;
  name: string;
  description: string;
  hosts: string[];
  participants: string[];
  createdAt: string;
  items: {
    itemId: number;
    itemName: string;
    criticality: number;
    accessibility: number;
    recoverability: number;
    vulnerability: number;
    effect: number;
    recognizability: number;
  }[];
  randomAssignment: boolean;
  roleBased: boolean;
  fivePointScoring: boolean;
  cMulti: number;
  aMulti: number;
  rMulti: number;
  vMulti: number;
  eMulti: number;
  r2Multi: number;
}

const ViewMatrix: React.FC = () => {
  const [matrices, setMatrices] = useState<CarverMatrix[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const navigate = useNavigate();
  const [roleFilters, setRoleFilters] = useState({
    host: false,
    participant: false,
    both: false,
  });
  const [roleBasedFilter, setRoleBasedFilter] = useState<'all' | 'enabled' | 'disabled'>('all');
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
      const url = "/api/carvermatrices/search";
      try {
        const response = await axios.get(url, { withCredentials: true });
        let matrixData;
        if (response.data.includes("[")) {
          const parts = response.data.split("]{");
          matrixData = JSON.parse(parts[0] + "]");
          setMatrices(matrixData);
        }
      } catch (error) {
        console.error("Error fetching matrices:", error);
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

  // Matrices can be filtered by name, description, or role
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
        matchesRole = isHost; // Show all matrices where user is a host, including those where they are both
      }
      if (roleFilters.participant) {
        matchesRole = matchesRole || isParticipant; // Show all matrices where user is a participant, including those where they are both
      }
      if (!roleFilters.host && !roleFilters.participant) {
        matchesRole = true; // No role filters selected
      }
    }

    console.log('Matrix:', matrix.name);
    console.log('Current user email:', userEmail);
    console.log('Hosts:', matrix.hosts);
    console.log('Is host:', isHost);
    console.log('Is participant:', isParticipant);
    console.log('Is both:', isBoth);
    console.log('Role filters:', roleFilters);
    console.log('Matches role:', matchesRole);
    console.log('Matches search:', matchesSearch);
    console.log('Final result:', matchesSearch && matchesRole);

    return matchesSearch && matchesRole;
  });

  const transformItemsForPdf = (items: CarverMatrix['items']) => {
    return items.map(item => {
      const getAverageScore = (scores: any) => {
        if (typeof scores === 'number') return scores;
        const values = Object.values(scores || {}).filter(score => score !== undefined && score !== null) as number[];
        return values.length > 0 ? values.reduce((sum, score) => sum + score, 0) / values.length : 0;
      };

      return {
        itemName: item.itemName,
        criticality: { default: getAverageScore(item.criticality) },
        accessibility: { default: getAverageScore(item.accessibility) },
        recoverability: { default: getAverageScore(item.recoverability) },
        vulnerability: { default: getAverageScore(item.vulnerability) },
        effect: { default: getAverageScore(item.effect) },
        recognizability: { default: getAverageScore(item.recognizability) }
      };
    });
  };

  return (
    <Box 
      sx={{
        display: "flex",
        minHeight: "100vh",
        backgroundColor: "#1a1a1a",
        color: "#ffffff",
        position: "relative",
      }}
    >
      {/* Background Pattern */}
      <Box
        sx={{
          position: "fixed",
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundImage: "linear-gradient(rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0.7)), url('/military-pattern.svg')",
          backgroundSize: "100px 100px",
          backgroundPosition: "center",
          opacity: 0.1,
          zIndex: 0,
        }}
      />

      {/* Sidebar Filters */}
      <Paper
        sx={{
          width: "300px",
          padding: 2,
          backgroundColor: "rgba(255, 255, 255, 0.05)",
          backdropFilter: "blur(10px)",
          border: "1px solid rgba(255, 255, 255, 0.1)",
          height: "100vh",
          display: "flex",
          flexDirection: "column",
          gap: 2,
          position: "relative",
          zIndex: 1,
        }}
      >
        <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
          <Typography
            variant="h6"
            sx={{
              color: "#ffffff",
              fontWeight: "bold",
              textTransform: "uppercase",
              letterSpacing: "1px",
              fontFamily: "'Roboto Condensed', sans-serif",
              mb: 1,
            }}
          >
            Matrices
          </Typography>

          <TextField
            fullWidth
            variant="outlined"
            placeholder="Search matrices..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon sx={{ color: 'rgba(255, 255, 255, 0.7)' }} />
                </InputAdornment>
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
            }}
          />

          <Box>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
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
                Filter by Role
              </Typography>
            </Box>
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
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
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
                Role-Based Matrix Filter
              </Typography>
            </Box>
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

        <Box sx={{ mt: 1, pt: 1, borderTop: '1px solid rgba(255, 255, 255, 0.1)' }}>
          <Button
            variant="contained"
            onClick={() => navigate(ROUTES.createMatrix)}
            fullWidth
            startIcon={<AddIcon />}
            sx={{
              backgroundColor: '#014093',
              color: '#ffffff',
              textTransform: 'uppercase',
              fontWeight: 'bold',
              letterSpacing: '1px',
              padding: '8px 0',
              '&:hover': {
                backgroundColor: '#012B61',
              },
            }}
          >
            Create Matrix
          </Button>
        </Box>
      </Paper>

      {/* Main Content */}
      <Box
        sx={{
          flex: 1,
          padding: 3,
          position: "relative",
          zIndex: 1,
          overflow: "auto",
        }}
      >
        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))",
            gap: 2,
            alignContent: "start",
          }}
        >
          {filteredMatrices.length > 0 ? (
            filteredMatrices.map((matrix) => (
              <Paper
                key={matrix.matrixId}
                sx={{
                  p: 2,
                  backgroundColor: "rgba(255, 255, 255, 0.05)",
                  backdropFilter: "blur(10px)",
                  border: "1px solid rgba(255, 255, 255, 0.1)",
                  transition: "all 0.2s ease-in-out",
                  display: "flex",
                  flexDirection: "column",
                  minHeight: "200px",
                  '&:hover': {
                    transform: 'translateY(-2px)',
                    backgroundColor: "rgba(255, 255, 255, 0.08)",
                    boxShadow: '0 4px 8px rgba(0, 0, 0, 0.2)',
                  },
                }}
              >
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                  <Typography
                    variant="h6"
                    sx={{
                      color: "#ffffff",
                      fontWeight: "bold",
                      fontFamily: "'Roboto Condensed', sans-serif",
                    }}
                  >
                    {matrix.name}
                  </Typography>
                  <Typography
                    variant="caption"
                    sx={{
                      color: "rgba(255, 255, 255, 0.5)",
                      whiteSpace: "nowrap",
                    }}
                  >
                    {new Date(matrix.createdAt).toLocaleDateString()}
                  </Typography>
                </Box>

                <Typography
                  variant="body2"
                  sx={{
                    color: "rgba(255, 255, 255, 0.7)",
                    mb: 2,
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                    display: "-webkit-box",
                    WebkitLineClamp: 3,
                    WebkitBoxOrient: "vertical",
                    flex: 1,
                    minHeight: "4.5em",
                  }}
                >
                  {matrix.description || "No description"}
                </Typography>

                <Box sx={{ display: 'flex', gap: 1, mb: 2, flexWrap: 'wrap' }}>
                  <Typography
                    variant="caption"
                    sx={{
                      color: "rgba(255, 255, 255, 0.5)",
                      backgroundColor: "rgba(255, 255, 255, 0.1)",
                      padding: "2px 8px",
                      borderRadius: "12px",
                    }}
                  >
                    {matrix.items.length} {matrix.items.length === 1 ? 'Target' : 'Targets'}
                  </Typography>
                  {matrix.randomAssignment && (
                    <Typography
                      variant="caption"
                      sx={{
                        color: "rgba(255, 255, 255, 0.5)",
                        backgroundColor: "rgba(255, 255, 255, 0.1)",
                        padding: "2px 8px",
                        borderRadius: "12px",
                      }}
                    >
                      Random
                    </Typography>
                  )}
                  {matrix.roleBased && (
                    <Typography
                      variant="caption"
                      sx={{
                        color: "rgba(255, 255, 255, 0.5)",
                        backgroundColor: "rgba(255, 255, 255, 0.1)",
                        padding: "2px 8px",
                        borderRadius: "12px",
                      }}
                    >
                      Role Based
                    </Typography>
                  )}
                  {matrix.fivePointScoring ? (
                    <Typography
                      variant="caption"
                      sx={{
                        color: "rgba(255, 255, 255, 0.5)",
                        backgroundColor: "rgba(255, 255, 255, 0.1)",
                        padding: "2px 8px",
                        borderRadius: "12px",
                      }}
                    >
                      5-Point Scale
                    </Typography>
                  ) : (
                    <Typography
                      variant="caption"
                      sx={{
                        color: "rgba(255, 255, 255, 0.5)",
                        backgroundColor: "rgba(255, 255, 255, 0.1)",
                        padding: "2px 8px",
                        borderRadius: "12px",
                      }}
                    >
                      10-Point Scale
                    </Typography>
                  )}
                </Box>

                <Box
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    mt: "auto",
                    pt: 1,
                  }}
                >
                  <Typography
                    variant="caption"
                    sx={{
                      color: matrix.hosts.includes(userEmail || "") && matrix.participants.includes(userEmail || "")
                        ? "#4D9FFF"
                        : matrix.hosts.includes(userEmail || "")
                        ? "#4D9FFF"
                        : "#00E676",
                      textTransform: "uppercase",
                      letterSpacing: "0.5px",
                      fontWeight: "bold",
                      textShadow: "0 0 10px rgba(255, 255, 255, 0.1)",
                    }}
                  >
                    {matrix.roleBased ? (
                      matrix.hosts.includes(userEmail || "") && matrix.participants.includes(userEmail || "")
                        ? "Host & Participant"
                        : matrix.hosts.includes(userEmail || "")
                        ? "Host"
                        : "Participant"
                    ) : null}
                  </Typography>
                  <Box sx={{ display: "flex", gap: 1 }}>
                    {matrix.hosts.includes(userEmail || "") && (
                      <>
                        <ExportPdfButton 
                          config={{
                            name: matrix.name,
                            description: matrix.description,
                            randomAssignment: matrix.randomAssignment,
                            roleBased: matrix.roleBased,
                            fivePointScoring: matrix.fivePointScoring,
                            cMulti: matrix.cMulti,
                            aMulti: matrix.aMulti,
                            rMulti: matrix.rMulti,
                            vMulti: matrix.vMulti,
                            eMulti: matrix.eMulti,
                            r2Multi: matrix.r2Multi
                          }} 
                          items={transformItemsForPdf(matrix.items)} 
                        />
                        <Tooltip title="Delete Matrix">
                          <IconButton
                            size="small"
                            sx={{
                              color: '#ff4444',
                              '&:hover': {
                                backgroundColor: 'rgba(255, 68, 68, 0.1)',
                              },
                            }}
                          >
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </>
                    )}
                    <Tooltip title="Open Matrix">
                      <IconButton
                        size="small"
                        onClick={(e) => {
                          e.stopPropagation();
                          const url = `/EditMatrix?matrixId=${matrix.matrixId}`;
                          navigate(url);
                        }}
                        sx={{
                          color: '#4D9FFF',
                          '&:hover': {
                            backgroundColor: 'rgba(77, 159, 255, 0.1)',
                          },
                        }}
                      >
                        <OpenInNewIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </Box>
                </Box>
              </Paper>
            ))
          ) : (
            <Typography
              sx={{
                color: "rgba(255, 255, 255, 0.7)",
                textAlign: "center",
                gridColumn: "1 / -1",
              }}
            >
              No matrices found.
            </Typography>
          )}
        </Box>
      </Box>
    </Box>
  );
};

export default ViewMatrix;
