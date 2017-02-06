/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.monitor.server.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.datacleaner.monitor.scheduling.SchedulingService;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinitionModel;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ScheduleDefinitionsControllerTest {
    private MockMvc _mockMvc;

    private SchedulingService _schedulingService;

    @Before
    public void setUp() throws Exception {
        _schedulingService = mock(SchedulingService.class);
        
        final ScheduleDefinitionsController scheduleDefinitionsController =
                new ScheduleDefinitionsController(_schedulingService);

        _mockMvc = MockMvcBuilders.standaloneSetup(scheduleDefinitionsController).build();
    }

    @Test
    public void testGetSchedule() throws Exception {
        final ScheduleDefinition scheduleDefinition = new ScheduleDefinition();
        scheduleDefinition.setHotFolder("/hotfolder");

        when(_schedulingService.getSchedule(argThat(tenant -> tenant.getId().equals("test")),
                argThat(job -> job.getName().equals("myjob"))))
                .thenReturn(scheduleDefinition);

        _mockMvc.perform(get("/test/schedules/myjob")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[?(@.hotFolder=='/hotfolder')]", hasSize(1)));
    }

    @Test
    public void testPutSchedule() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();
        final ScheduleDefinition scheduleDefinition = new ScheduleDefinition();
        final ScheduleDefinitionModel scheduleDefinitionModel = new ScheduleDefinitionModel("/hotfolder");

        when(_schedulingService.getSchedule(argThat(tenant -> tenant.getId().equals("test")),
                argThat(job -> job.getName().equals("myjob")))).thenReturn(scheduleDefinition);

        _mockMvc.perform(put("/test/schedules/myjob").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(scheduleDefinitionModel))).andExpect(status().isCreated())
                .andExpect(header().string("Location", is("http://localhost/test/schedules/myjob"))).andReturn();

        verify(_schedulingService, times(1)).updateSchedule(argThat(tenant -> tenant.getId().equals("test")),
                eq(scheduleDefinition));
    }
}
