package dev.tippspiel.betting.application.settlement;

import dev.tippspiel.betting.domain.model.BettingGroup;
import dev.tippspiel.betting.domain.model.Participation;
import dev.tippspiel.betting.domain.repository.BettingGroupRepository;
import dev.tippspiel.betting.domain.repository.ParticipationRepository;
import dev.tippspiel.betting.domain.service.PrizeDistributionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service that settles a BettingGroup at tournament end:
 *
 *   1. Load all Participations for the group
 *   2. Distribute prizes via PrizeDistributionService (power-tower algorithm)
 *   3. Persist all updated Participations
 *   4. Close the BettingGroup (prevents further tipping / joining)
 *
 * Idempotent: groups that are already CLOSED are skipped silently.
 */
@Service
public class SettlementService {

    private static final Logger log = LoggerFactory.getLogger(SettlementService.class);

    private final BettingGroupRepository  groupRepository;
    private final ParticipationRepository participationRepository;
    private final PrizeDistributionService prizeDistributionService;

    public SettlementService(BettingGroupRepository groupRepository,
                             ParticipationRepository participationRepository,
                             PrizeDistributionService prizeDistributionService) {
        this.groupRepository         = groupRepository;
        this.participationRepository = participationRepository;
        this.prizeDistributionService = prizeDistributionService;
    }

    /**
     * Settle a single betting group.
     *
     * @param bettingGroupId the group to settle
     * @throws IllegalArgumentException when the group is not found
     */
    @Transactional
    public void settle(UUID bettingGroupId) {
        BettingGroup group = groupRepository.findById(bettingGroupId)
                .orElseThrow(() -> new IllegalArgumentException("BettingGroup not found: " + bettingGroupId));

        if (!group.isOpen()) {
            log.info("BettingGroup {} is already closed — skipping settlement", bettingGroupId);
            return;
        }

        List<Participation> participations = participationRepository.findAllByGroup(bettingGroupId);

        if (participations.isEmpty()) {
            log.warn("BettingGroup {} has no participants — closing without prize distribution", bettingGroupId);
            group.close();
            groupRepository.save(group);
            return;
        }

        log.info("Settling BettingGroup {} with {} participants, stake={}{}",
                bettingGroupId,
                participations.size(),
                group.getStake().amount(),
                group.getStake().currency());

        prizeDistributionService.distribute(participations, group.getStake().amount());

        participationRepository.saveAll(participations);

        group.close();
        groupRepository.save(group);

        log.info("Settlement complete for BettingGroup {} — pot={}{}",
                bettingGroupId,
                group.getStake().amount().multiply(java.math.BigDecimal.valueOf(participations.size())),
                group.getStake().currency());
    }
}
